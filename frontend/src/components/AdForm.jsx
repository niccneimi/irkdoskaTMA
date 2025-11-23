import { useState } from 'react';
import PhotoGallery from './PhotoGallery';
import { useNotification } from '../contexts/NotificationContext';
import '../styles/form.css';

function AdForm() {
    const { showNotification } = useNotification();
    const [photos, setPhotos] = useState([]);
    const [formData, setFormData] = useState({
        description: '',
        city: '',
        phone: '',
        price: ''
    });

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

    const handleSubmit = (e) => {
        e.preventDefault();

        const submitData = {
            ...formData,
            photos: photos
        };

        console.log('Отправка объявления:', submitData);
        showNotification('Объявление добавлено и отправлено на модерацию', 'success');

        setFormData({
            description: '',
            city: '',
            phone: '',
            price: ''
        });
        setPhotos([]);
    };

    return (
        <div className="form-card">
            <PhotoGallery maxPhotos={5} onChange={setPhotos} />

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
                        placeholder="Например: Москва"
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

                <button type="submit" className="submit-button">
                    Добавить!
                </button>
            </form>
        </div>
    );
}

export default AdForm;
