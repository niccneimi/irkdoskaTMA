from dotenv import load_dotenv
import os

load_dotenv()

TG_BOT_TOKEN = os.getenv("TG_BOT_TOKEN")
DB_USERNAME = os.getenv("POSTGRES_USER")
DB_PASSWORD = os.getenv("POSTGRES_PASSWORD")
DB_HOST = os.getenv("DB_HOST")
DB_PORT = os.getenv("DB_PORT")
DB_NAME = os.getenv("POSTGRES_DB")
DOMAIN_NAME = os.getenv("DOMAIN_NAME")
PAYMENT_WEBHOOK_SECRET = os.getenv("PAYMENT_WEBHOOK_SECRET", "")
ADMIN_IDS = [718802381, 7724264827, 1899914568]
start_message = "Нажмите запустить для публикации объявления!"