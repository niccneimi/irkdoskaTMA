import { useState, useEffect } from 'react';
import PhotoGallery from './PhotoGallery';
import { useNotification } from '../contexts/NotificationContext';
import { retrieveRawInitData } from '@telegram-apps/sdk';
import axios from 'axios';
import '../styles/form.css';

function AdForm({ isPaid = false }) {
    const { showNotification, showError } = useNotification();
    const [photos, setPhotos] = useState([]);
    const [resetPhotosToken, setResetPhotosToken] = useState(0);
    const [isLoading, setIsLoading] = useState(false);
    const [balance, setBalance] = useState(null);
    const [formData, setFormData] = useState({
        description: '',
        city: '',
        phone: '',
        price: ''
    });

    useEffect(() => {
        if (isPaid) {
            loadBalance();
        }
    }, [isPaid]);

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
            setBalance(0);
        }
    };

    const formatPhone = (value) => {
        let digits = value.replace(/\D/g, '');
        
        if (digits.length > 0) {
            if (digits[0] === '7' || digits[0] === '8') {
                digits = '7' + digits.substring(1);
            } else {
                digits = '7' + digits;
            }
        }

        if (digits.length > 11) {
            digits = digits.substring(0, 11);
        }

        let formatted = '';
        if (digits.length > 0) {
            formatted = '+7';
            if (digits.length > 1) {
                formatted += ' (' + digits.substring(1, 4);
            }
            if (digits.length > 4) {
                formatted += ') ' + digits.substring(4, 7);
            }
            if (digits.length > 7) {
                formatted += '-' + digits.substring(7, 9);
            }
            if (digits.length > 9) {
                formatted += '-' + digits.substring(9, 11);
            }
        }

        return formatted;
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        
        if (name === 'phone') {
            setFormData(prev => ({
                ...prev,
                [name]: formatPhone(value)
            }));
        } else {
            setFormData(prev => ({
                ...prev,
                [name]: value
            }));
        }
    };

    async function createAd(dataRaw) {
        setIsLoading(true);

        const formDataToSend = new FormData();

        const adRequest = {
            description: formData.description,
            price: parseFloat(formData.price),
            city: formData.city,
            phone: formData.phone,
            isPaid: isPaid
        };

        formDataToSend.append('adRequest', new Blob([JSON.stringify(adRequest)], {
            type: 'application/json'
        }));

        if (photos && photos.length > 0) {
            photos.forEach((photo) => {
                formDataToSend.append('photos', photo);
            });
        }

        axios.post('/api/ads', formDataToSend, {
            headers: {
                'Authorization': 'tma ' + dataRaw
            }
        })
            .then(response => {
                showNotification('Объявление добавлено и отправлено на модерацию');
                setFormData({
                    description: '',
                    city: '',
                    phone: '',
                    price: ''
                });
                setPhotos([]);
                setResetPhotosToken(prev => prev + 1);
                if (isPaid) {
                    loadBalance();
                }
                setIsLoading(false);
            })
            .catch(error => {
                console.error('Create ad error: ', error);
                let errorMessage = 'Не удалось создать объявление. Попробуйте еще раз.';
                
                if (error.response?.data) {
                    errorMessage = error.response.data.message || 
                                  error.response.data.error || 
                                  (typeof error.response.data === 'string' ? error.response.data : errorMessage);
                } else if (error.message) {
                    errorMessage = error.message;
                }

                if (errorMessage.includes('Недостаточно платных объявлений') || 
                    errorMessage.includes('баланс') ||
                    error.response?.status === 402) {
                    if (isPaid) {
                        loadBalance();
                    }
                    showError('❌ ' + errorMessage + ' Перейдите в магазин и купите тариф.');
                } else {
                    showError(errorMessage);
                }
                setIsLoading(false);
            });
    }

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!formData.description || !formData.price || !formData.city || !formData.phone) {
            showError('Пожалуйста, заполните все обязательные поля');
            return;
        }

        if (isPaid) {
            if (balance === null) {
                showError('Загрузка баланса... Пожалуйста, подождите.');
                return;
            }
            if (balance <= 0) {
                showError('Недостаточно платных объявлений. Перейдите в магазин и купите тариф.');
                return;
            }
        }

        const dataRaw = retrieveRawInitData();
        createAd(dataRaw);
    };

    return (
        <div className="form-card">
            {isPaid && balance !== null && (
                <div className={`balance-info ${balance <= 0 ? 'balance-warning' : ''}`}>
                    {balance <= 0 ? (
                        <div>
                            <div>⚠️ У вас нет платных объявлений. Купите тариф в магазине.</div>
                            <div style={{ marginTop: '8px', fontSize: '0.9em', opacity: 0.8 }}>
                                Платное объявление: вакансии, услуги и прочие виды деятельности приносящие доход своим владельцам
                            </div>
                        </div>
                    ) : (
                        <div>
                            <div>💰 Доступно платных объявлений: <strong>{balance}</strong></div>
                        <div style={{ marginTop: '8px', fontSize: '0.9em', opacity: 0.8 }}>
                            Платное объявление: вакансии, услуги и прочие виды деятельности приносящие доход своим владельцам
                        </div>
                    </div>
                    )}
                </div>
            )}
            <PhotoGallery maxPhotos={5} onChange={setPhotos} resetToken={resetPhotosToken} />

            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label className="form-label" htmlFor="description">
                        Описание
                    </label>
                    <textarea
                        className="form-textarea"
                        id="description"
                        name="description"
                        placeholder="Опишите ваше объявление..."
                        value={formData.description}
                        onChange={handleInputChange}
                        required
                    />
                </div>

                <div className="form-group">
                    <label className="form-label" htmlFor="city">
                        Город (населенный пункт)
                    </label>
                    <input
                        type="text"
                        className="form-input"
                        id="city"
                        name="city"
                        placeholder="Например: Иркутск"
                        value={formData.city}
                        onChange={handleInputChange}
                        required
                    />
                </div>

                <div className="form-group">
                    <label className="form-label" htmlFor="phone">
                        Номер телефона для связи
                    </label>
                    <input
                        type="tel"
                        className="form-input"
                        id="phone"
                        name="phone"
                        placeholder="+7 (___) ___-__-__"
                        value={formData.phone}
                        onChange={handleInputChange}
                        required
                    />
                </div>

                <div className="form-group">
                    <label className="form-label" htmlFor="price">
                        Цена
                    </label>
                    <div className="price-group">
                        <input
                            type="number"
                            className="form-input price-input"
                            id="price"
                            name="price"
                            placeholder="0"
                            min="0"
                            value={formData.price}
                            onChange={handleInputChange}
                            required
                        />
                        <span className="price-label">₽</span>
                    </div>
                </div>

                <button 
                    type="submit" 
                    className="submit-button"
                    disabled={isLoading}
                >
                    {isLoading ? 'Отправка...' : 'Добавить!'}
                </button>
            </form>
        </div>
    );
}

export default AdForm;
