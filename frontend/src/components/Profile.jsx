import { useEffect, useState } from 'react';
import axios from 'axios';
import { retrieveRawInitData } from '@telegram-apps/sdk';
import '../styles/profile.css';

function Profile() {
    const [ads, setAds] = useState([]);
    const [selectedAd, setSelectedAd] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
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

        loadAds();
    }, []);

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

    return (
        <div className="profile-layout">
            <div className="profile-list">
                <h2 className="profile-title">История объявлений</h2>

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
    );
}

export default Profile;


