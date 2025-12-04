import aiogram
from aiogram.types import InlineKeyboardMarkup, InlineKeyboardButton, InlineQueryResultArticle, InputTextMessageContent
from aiogram.methods import SavePreparedInlineMessage
from aiogram.types.web_app_info import WebAppInfo
from aiogram.filters import CommandStart, CommandObject, StateFilter
from aiogram import F
from aiogram.fsm.context import FSMContext
from aiogram.fsm.state import State, StatesGroup
from aiogram.fsm.storage.memory import MemoryStorage
import logging, asyncio
import aiohttp
import json
from config import *
from markups import *
from db import Database

bot = aiogram.Bot(TG_BOT_TOKEN)
storage = MemoryStorage()
dp = aiogram.Dispatcher(storage=storage)
db = Database(f'postgresql://{DB_USERNAME}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}')
BACKEND_URL = f"https://{DOMAIN_NAME}/api/moderation"
PAYMENT_BACKEND_URL = f"https://{DOMAIN_NAME}/api/payments"

class RejectState(StatesGroup):
    waiting_for_reason = State()

@dp.message(CommandStart())
async def start(message: aiogram.types.Message):
    startKeyboard = [[InlineKeyboardButton(text="Запустить", web_app=WebAppInfo(url=f"https://{DOMAIN_NAME}/"))]]
    markup = InlineKeyboardMarkup(inline_keyboard=startKeyboard)
    await message.answer(start_message, reply_markup=markup)

@dp.callback_query(F.data.startswith("approve_"))
async def approve_ad(callback: aiogram.types.CallbackQuery):
    if callback.from_user.id not in ADMIN_IDS:
        await callback.answer("У вас нет прав для модерации")
        return
    
    ad_id = callback.data.split("_")[1]
    try:
        async with aiohttp.ClientSession() as session:
            async with session.post(
                f"{BACKEND_URL}/bot/{ad_id}/approve",
                params={"telegramId": callback.from_user.id}
            ) as resp:
                if resp.status == 200:
                    await callback.message.edit_reply_markup(reply_markup=None)
                    await callback.message.reply("✅ Объявление одобрено")
                else:
                    await callback.answer("Ошибка при одобрении объявления")
    except Exception as e:
        logging.error(f"Error approving ad {ad_id}: {e}")
        await callback.answer("Ошибка при одобрении объявления")

@dp.callback_query(F.data.startswith("reject_"))
async def reject_ad(callback: aiogram.types.CallbackQuery, state: FSMContext):
    if callback.from_user.id not in ADMIN_IDS:
        await callback.answer("У вас нет прав для модерации")
        return
    
    ad_id = callback.data.split("_")[1]
    
    empty_keyboard = InlineKeyboardMarkup(inline_keyboard=[])
    try:
        if callback.message:
            await bot.edit_message_reply_markup(
                chat_id=callback.message.chat.id,
                message_id=callback.message.message_id,
                reply_markup=empty_keyboard
            )
    except Exception as e:
        logging.warning(f"Could not remove keyboard for reject: {e}")
    
    await callback.answer()
    await state.set_state(RejectState.waiting_for_reason)
    await state.update_data(ad_id=ad_id)
    if callback.message:
        await callback.message.reply("Пожалуйста, укажите причину отказа:")

@dp.message(StateFilter(RejectState.waiting_for_reason))
async def process_reject_reason(message: aiogram.types.Message, state: FSMContext):
    if message.from_user.id not in ADMIN_IDS:
        await state.clear()
        return
    
    data = await state.get_data()
    ad_id = data.get("ad_id")
    reason = message.text
    
    try:
        async with aiohttp.ClientSession() as session:
            async with session.post(
                f"{BACKEND_URL}/bot/{ad_id}/reject",
                params={"telegramId": message.from_user.id},
                json={"reason": reason}
            ) as resp:
                if resp.status == 200:
                    await message.reply("❌ Объявление отклонено")
                else:
                    await message.reply("Ошибка при отклонении объявления")
    except Exception as e:
        logging.error(f"Error rejecting ad {ad_id}: {e}")
        await message.reply("Ошибка при отклонении объявления")
    finally:
        await state.clear()

@dp.callback_query(F.data.startswith("commercial_"))
async def commercial_ad(callback: aiogram.types.CallbackQuery):
    if callback.from_user.id not in ADMIN_IDS:
        await callback.answer("У вас нет прав для модерации")
        return
    
    ad_id = callback.data.split("_")[1]
    try:
        async with aiohttp.ClientSession() as session:
            async with session.post(
                f"{BACKEND_URL}/bot/{ad_id}/commercial",
                params={"telegramId": callback.from_user.id}
            ) as resp:
                if resp.status == 200:
                    await callback.message.edit_reply_markup(reply_markup=None)
                    await callback.message.reply("💳 Объявление отклонено как коммерческое")
                else:
                    await callback.answer("Ошибка при обработке объявления")
    except Exception as e:
        logging.error(f"Error marking ad {ad_id} as commercial: {e}")
        await callback.answer("Ошибка при обработке объявления")

@dp.pre_checkout_query()
async def process_pre_checkout_query(pre_checkout_query: aiogram.types.PreCheckoutQuery):
    await bot.answer_pre_checkout_query(pre_checkout_query.id, ok=True)

@dp.message(F.content_type == aiogram.enums.ContentType.SUCCESSFUL_PAYMENT)
async def process_successful_payment(message: aiogram.types.Message):
    successful_payment = message.successful_payment
    invoice_payload = successful_payment.invoice_payload
    telegram_id = message.from_user.id
    
    logging.info(f"Processing successful payment. Invoice payload: {invoice_payload}, Telegram ID: {telegram_id}")
    
    if not PAYMENT_WEBHOOK_SECRET:
        logging.error("PAYMENT_WEBHOOK_SECRET not configured in bot")
        await message.answer("⚠️ Ошибка конфигурации сервера. Обратитесь в поддержку.")
        return
    
    try:
        headers = {
            "Content-Type": "application/json",
            "X-Payment-Token": PAYMENT_WEBHOOK_SECRET
        }
        
        async with aiohttp.ClientSession() as session:
            async with session.post(
                f"{PAYMENT_BACKEND_URL}/success",
                json={
                    "invoice_payload": invoice_payload,
                    "telegram_id": telegram_id
                },
                headers=headers
            ) as resp:
                response_text = await resp.text()
                logging.info(f"Backend response status: {resp.status}, body: {response_text}")
                
                if resp.status == 200:
                    await message.answer("✅ Платеж успешно обработан! Баланс обновлен.")
                else:
                    error_msg = f"⚠️ Платеж получен, но возникла ошибка при обработке (код: {resp.status}). Обратитесь в поддержку."
                    logging.error(f"Backend returned error status {resp.status}: {response_text}")
                    await message.answer(error_msg)
    except Exception as e:
        logging.error(f"Error processing successful payment: {e}", exc_info=True)
        await message.answer("⚠️ Платеж получен, но возникла ошибка при обработке. Обратитесь в поддержку.")

async def main():
    await bot.delete_webhook(True)
    await db.create_pool()
    try:
        await dp.start_polling(bot, allowed_updates=["message", "inline_query", "callback_query", "pre_checkout_query"])
    finally:
        await db.close_pool()

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    asyncio.run(main())
