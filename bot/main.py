import aiogram
from aiogram.types import InlineKeyboardMarkup, InlineKeyboardButton, InlineQueryResultArticle, InputTextMessageContent
from aiogram.methods import SavePreparedInlineMessage
from aiogram.types.web_app_info import WebAppInfo
from aiogram.filters import CommandStart, CommandObject
import logging, asyncio
from config import *
from markups import *
from db import Database

bot = aiogram.Bot(TG_BOT_TOKEN)
dp = aiogram.Dispatcher()
db = Database(f'postgresql://{DB_USERNAME}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}')

@dp.message(CommandStart())
async def start(message: aiogram.types.Message):
    startKeyboard = [[InlineKeyboardButton(text="Запустить", web_app=WebAppInfo(url=f"https://{DOMAIN_NAME}/"))]]
    markup = InlineKeyboardMarkup(inline_keyboard=startKeyboard)
    await message.answer(start_message, reply_markup=markup)

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
