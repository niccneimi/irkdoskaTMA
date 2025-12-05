import { useEffect, useState } from 'react';
import axios from 'axios';
import { retrieveRawInitData } from '@telegram-apps/sdk';
import '../styles/profile.css';

function Profile() {
    const [activeSection, setActiveSection] = useState('history');
    const [ads, setAds] = useState([]);
    const [selectedAd, setSelectedAd] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [balance, setBalance] = useState(0);
    const [packages, setPackages] = useState([]);
    const [isLoadingShop, setIsLoadingShop] = useState(false);

    useEffect(() => {
        const checkTelegramAPI = () => {
            if (window.Telegram?.WebApp) {
                console.log('Telegram WebApp API доступен');
            } else {
                console.warn('Telegram WebApp API не найден. Убедитесь, что приложение открыто через Telegram.');
                setTimeout(() => {
                    if (window.Telegram?.WebApp) {
                        console.log('Telegram WebApp API загружен');
                    } else {
                        console.error('Telegram WebApp API все еще недоступен');
                    }
                }, 1000);
            }
        };

        const loadAds = async () => {
            setIsLoading(true);
            setError(null);
            try {
                const dataRaw = retrieveRawInitData();
                const response = await axios.get('/api/ads', {
                    headers: {
                        'Authorization': 'tma ' + dataRaw
                    }
                });
                const list = response.data?.ads || [];
                setAds(list);
                if (list.length > 0) {
                    setSelectedAd(list[0]);
                }
            } catch (e) {
                console.error('Failed to load ads history', e);
                setError('Не удалось загрузить историю объявлений');
            } finally {
                setIsLoading(false);
            }
        };

        checkTelegramAPI();
        loadAds();
        loadBalance();
        loadPackages();
    }, []);

    const loadBalance = async () => {
        try {
            const dataRaw = retrieveRawInitData();
            const response = await axios.get('/api/shop/balance', {
                headers: {
                    'Authorization': 'tma ' + dataRaw
                }
            });
            setBalance(response.data?.balance || 0);
        } catch (e) {
            console.error('Failed to load balance', e);
        }
    };

    const loadPackages = async () => {
        try {
            const response = await axios.get('/api/shop/packages');
            setPackages(response.data || []);
        } catch (e) {
            console.error('Failed to load packages', e);
        }
    };

    const purchasePackage = async (packageId) => {
        setIsLoadingShop(true);

        try {
            const dataRaw = retrieveRawInitData();
            const response = await axios.post(`/api/shop/invoice/${packageId}`, {}, {
                headers: {
                    'Authorization': 'tma ' + dataRaw,
                    'Content-Type': 'application/json'
                },
                responseType: 'text'
            });
            
            if (response.data.startsWith('ERROR:')) {
                alert('Ошибка: ' + response.data.substring(6));
                setIsLoadingShop(false);
                return;
            }

            let tg = window.Telegram?.WebApp;
            
            if (!tg) {
                await new Promise(resolve => setTimeout(resolve, 100));
                tg = window.Telegram?.WebApp;
            }
            
            if (!tg) {
                setIsLoadingShop(false);
                console.error('Telegram WebApp API not available', {
                    windowTelegram: !!window.Telegram,
                    telegramWebApp: !!window.Telegram?.WebApp,
                    userAgent: navigator.userAgent
                });
                return;
            }

            if (typeof tg.openInvoice !== 'function') {
                setIsLoadingShop(false);
                console.error('openInvoice method not available', {
                    tg: !!tg,
                    methods: Object.keys(tg || {}),
                    openInvoice: typeof tg.openInvoice,
                    version: tg.version
                });
                alert('Метод openInvoice недоступен. Проверьте версию Telegram WebApp API.');
                return;
            }

            if (typeof tg.expand === 'function') {
                tg.expand();
            }
            
            if (typeof tg.ready === 'function') {
                tg.ready();
            }
            
            const root = document.getElementById('root');
            const html = document.documentElement;
            const body = document.body;
            
            const originalRootDisplay = root ? root.style.display : '';
            const originalHtmlHeight = html.style.height;
            const originalBodyHeight = body.style.height;
            const originalHtmlOverflow = html.style.overflow;
            const originalBodyOverflow = body.style.overflow;
            
            if (root) {
                root.style.display = 'none';
            }
            
            html.style.height = '100vh';
            html.style.overflow = 'hidden';
            body.style.height = '100vh';
            body.style.overflow = 'hidden';
            
            if (tg.viewportHeight) {
                const vh = tg.viewportHeight;
                html.style.height = vh + 'px';
                body.style.height = vh + 'px';
            }
            
            const restoreViewport = () => {
                if (root) {
                    root.style.display = originalRootDisplay;
                }
                html.style.height = originalHtmlHeight;
                body.style.height = originalBodyHeight;
                html.style.overflow = originalHtmlOverflow;
                body.style.overflow = originalBodyOverflow;
            };

            tg.openInvoice(response.data, (status) => {
                restoreViewport();
                
                setIsLoadingShop(false);
                if (status === 'paid') {
                    loadBalance();
                    alert('✅ Платеж успешно выполнен! Баланс обновлен.');
                } else if (status === 'failed') {
                    alert('❌ Ошибка при оплате. Попробуйте еще раз.');
                } else if (status === 'cancelled' || status === 'pending') {
                    restoreViewport();
            }
            });
        } catch (e) {
            const html = document.documentElement;
            const body = document.body;
            const root = document.getElementById('root');
            
            html.style.height = '';
            html.style.overflow = '';
            html.style.position = '';
            html.style.width = '';
            html.style.top = '';
            html.style.left = '';
            
            body.style.height = '';
            body.style.overflow = '';
            body.style.position = '';
            body.style.width = '';
            body.style.margin = '';
            body.style.padding = '';
            
            if (root) {
                root.style.display = '';
            }
            
            setIsLoadingShop(false);
            console.error('Failed to create invoice', e);
            const errorMsg = e.response?.data || e.message || 'Не удалось создать платеж';
            alert('Ошибка: ' + errorMsg);
        }
    };

    const formatDate = (value) => {
        if (!value) return '';
        try {
            const d = new Date(value);
            return d.toLocaleString('ru-RU', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch {
            return value;
        }
    };

    const getStatusText = (status) => {
        switch (status) {
            case 'PENDING':
                return 'Модерируется';
            case 'APPROVED':
                return 'Одобрено';
            case 'REJECTED':
                return 'Отклонено';
            default:
                return 'Модерируется';
        }
    };

    const getStatusClass = (status) => {
        switch (status) {
            case 'PENDING':
                return 'status-pending';
            case 'APPROVED':
                return 'status-approved';
            case 'REJECTED':
                return 'status-rejected';
            default:
                return 'status-pending';
        }
    };

    const getAdsCountText = (count) => {
        const lastDigit = count % 10;
        const lastTwoDigits = count % 100;
        
        if (lastTwoDigits >= 11 && lastTwoDigits <= 14) {
            return `${count} объявлений`;
        }
        
        if (lastDigit === 1) {
            return `${count} объявление`;
        }
        
        if (lastDigit >= 2 && lastDigit <= 4) {
            return `${count} объявления`;
        }
        
        return `${count} объявлений`;
    };

    return (
        <div className="profile-container">
            <div className="profile-tabs">
                <div
                    className={`profile-tab ${activeSection === 'history' ? 'active' : ''}`}
                    onClick={() => setActiveSection('history')}
                >
                    История
                </div>
                <div
                    className={`profile-tab ${activeSection === 'shop' ? 'active' : ''}`}
                    onClick={() => setActiveSection('shop')}
                >
                    Магазин
                </div>
            </div>

            {activeSection === 'history' && (
                <div className="profile-layout">
                    <div className="profile-list">
                        <h2 className="profile-title">История объявлений</h2>
                        <div className="profile-balance-info">
                            Баланс платных объявлений: <strong>{balance}</strong>
                        </div>

                {isLoading && <div className="profile-info">Загрузка...</div>}
                {error && <div className="profile-error">{error}</div>}
                {!isLoading && !error && ads.length === 0 && (
                    <div className="profile-info">У вас пока нет объявлений</div>
                )}

                <ul className="profile-ads-list">
                    {ads.map((ad) => (
                        <li
                            key={ad.id}
                            className={
                                'profile-ads-item' +
                                (selectedAd && selectedAd.id === ad.id ? ' active' : '')
                            }
                            onClick={() => setSelectedAd(ad)}
                        >
                            <div className="profile-ads-main">
                                <div className="profile-ads-description">
                                    {ad.description}
                                </div>
                                <div className="profile-ads-meta">
                                    <span>{ad.city}</span>
                                    {ad.price != null && (
                                        <span className="profile-ads-price">
                                            {ad.price} ₽
                                        </span>
                                    )}
                                </div>
                            </div>
                            <div className="profile-ads-footer">
                                <div className={`profile-ads-status ${getStatusClass(ad.moderationStatus)}`}>
                                    {getStatusText(ad.moderationStatus)}
                                </div>
                                <div className="profile-ads-date">
                                    {formatDate(ad.createdAt)}
                                </div>
                            </div>
                        </li>
                    ))}
                </ul>
            </div>

            {selectedAd && (
                <div className="profile-details">
                    <h3 className="profile-details-title">Детали объявления</h3>
                    <div className="profile-details-block">
                        <div className="profile-details-row">
                            <span className="label">Описание:</span>
                            <span>{selectedAd.description}</span>
                        </div>
                        <div className="profile-details-row">
                            <span className="label">Город:</span>
                            <span>{selectedAd.city}</span>
                        </div>
                        <div className="profile-details-row">
                            <span className="label">Телефон:</span>
                            <span>{selectedAd.phone}</span>
                        </div>
                        <div className="profile-details-row">
                            <span className="label">Цена:</span>
                            <span>
                                {selectedAd.price != null ? `${selectedAd.price} ₽` : '—'}
                            </span>
                        </div>
                        <div className="profile-details-row">
                            <span className="label">Статус:</span>
                            <span className={`profile-status-badge ${getStatusClass(selectedAd.moderationStatus)}`}>
                                {getStatusText(selectedAd.moderationStatus)}
                            </span>
                        </div>
                        <div className="profile-details-row">
                            <span className="label">Создано:</span>
                            <span>{formatDate(selectedAd.createdAt)}</span>
                        </div>
                        {selectedAd.rejectionReason && (
                            <div className="profile-details-row">
                                <span className="label">Причина отказа:</span>
                                <span style={{ color: '#ff4444' }}>{selectedAd.rejectionReason}</span>
                            </div>
                        )}
                    </div>

                    {selectedAd.photoUrls && selectedAd.photoUrls.length > 0 && (
                        <div className="profile-photos">
                            <div className="profile-photos-title">Фотографии</div>
                            <div className="profile-photos-grid">
                                {selectedAd.photoUrls.map((path, index) => {
                                    const src = '/api/photos?path=' + encodeURIComponent(path);
                                    return (
                                    <div className="profile-photo-wrapper" key={index}>
                                        <img
                                            src={src}
                                            alt={`Фото ${index + 1}`}
                                            className="profile-photo"
                                        />
                                    </div>
                                    );
                                })}
                            </div>
                        </div>
                    )}
                </div>
            )}
            </div>
            )}

            {activeSection === 'shop' && (
                <div className="shop-section">
                    <h2 className="profile-title">Магазин платных объявлений</h2>
                    <div className="profile-balance-info">
                        Баланс платных объявлений: <strong>{balance}</strong>
                    </div>
                    <div className="packages-grid">
                        {packages.map((pkg) => (
                            <div key={pkg.id} className="package-card">
                                <div className="package-name">{pkg.name}</div>
                                <div className="package-count">{getAdsCountText(pkg.adsCount)}</div>
                                <div className="package-price">{pkg.price} ₽</div>
                                <button
                                    className="package-buy-button"
                                    onClick={() => purchasePackage(pkg.id)}
                                    disabled={isLoadingShop}
                                >
                                    {isLoadingShop ? 'Обработка...' : 'Купить'}
                                </button>
                            </div>
                        ))}
                    </div>
                    {packages.length === 0 && !isLoadingShop && (
                        <div className="profile-info">Тарифы временно недоступны</div>
                    )}
                </div>
            )}
        </div>
    );
}

export default Profile;


