from aiogram.types import ReplyKeyboardMarkup, KeyboardButton, InlineKeyboardMarkup, InlineKeyboardButton
from config import *

admin_keyboard = InlineKeyboardMarkup(
    inline_keyboard=[
        [
            InlineKeyboardButton(text="✅ Опубликовать", callback_data="approve"),
            InlineKeyboardButton(text="❌ Отклонить", callback_data="reject")
        ],
        [
            InlineKeyboardButton(text="✏️ Редактировать", callback_data="edit"),
            InlineKeyboardButton(text="💳 Коммерческое", callback_data="commercial")
        ]
    ]
)

commercial_admin_keyboard = InlineKeyboardMarkup(
    inline_keyboard=[
        [
            InlineKeyboardButton(text="✅ Опубликовать", callback_data="approve"),
            InlineKeyboardButton(text="❌ Отклонить", callback_data="reject")
        ],
        [
            InlineKeyboardButton(text="✏️ Редактировать", callback_data="edit")
        ]
    ]
)