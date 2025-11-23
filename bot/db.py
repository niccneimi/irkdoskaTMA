import asyncpg
import logging

class Database:
    def __init__(self, dsn: str):
        self.dsn = dsn
        self._pool = None

    async def create_pool(self):
        self._pool = await asyncpg.create_pool(self.dsn)

    async def close_pool(self):
        if self._pool:
            await self._pool.close()