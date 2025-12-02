import aiogram
from aiogram.types import InlineKeyboardMarkup, InlineKeyboardButton, InlineQueryResultArticle, InputTextMessageContent
from aiogram.methods import SavePreparedInlineMessage
from aiogram.types.web_app_info import WebAppInfo
from aiogram.filters import CommandStart, CommandObject
from aiogram import F
import logging, asyncio
import aiohttp
from config import *
from markups import *
from db import Database

bot = aiogram.Bot(TG_BOT_TOKEN)
dp = aiogram.Dispatcher()
db = Database(f'postgresql://{DB_USERNAME}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}')
BACKEND_URL = f"https://{DOMAIN_NAME}/api/moderation"

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
async def reject_ad(callback: aiogram.types.CallbackQuery):
    if callback.from_user.id not in ADMIN_IDS:
        await callback.answer("У вас нет прав для модерации")
        return
    
    ad_id = callback.data.split("_")[1]
    try:
        async with aiohttp.ClientSession() as session:
            async with session.post(
                f"{BACKEND_URL}/bot/{ad_id}/reject",
                params={"telegramId": callback.from_user.id}
            ) as resp:
                if resp.status == 200:
                    await callback.answer("Объявление отклонено ❌")
                    await callback.message.edit_reply_markup(reply_markup=None)
                    await callback.message.reply("❌ Объявление отклонено")
                else:
                    await callback.answer("Ошибка при отклонении объявления")
    except Exception as e:
        logging.error(f"Error rejecting ad {ad_id}: {e}")
        await callback.answer("Ошибка при отклонении объявления")

async def main():
    await bot.delete_webhook(True)
    await db.create_pool()
    try:
        await dp.start_polling(bot,allowed_updates=["message", "inline_query", "callback_query"])
    finally:
        await db.close_pool()

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    asyncio.run(main())
