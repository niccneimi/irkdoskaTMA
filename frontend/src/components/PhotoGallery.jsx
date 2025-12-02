import { useEffect, useState } from 'react';
import '../styles/gallery.css';

function PhotoGallery({ maxPhotos = 5, onChange, resetToken }) {
    const [photos, setPhotos] = useState([]);

    const handleFileSelect = (e) => {
        const files = Array.from(e.target.files);
        const remainingSlots = maxPhotos - photos.length;
        const filesToAdd = files.slice(0, remainingSlots);

        const readers = filesToAdd.map(file => {
            return new Promise((resolve) => {
                const reader = new FileReader();
                reader.onload = (e) => resolve({
                    file: file,
                    preview: e.target.result
                });
                reader.readAsDataURL(file);
            });
        });

        Promise.all(readers).then(newPhotos => {
            const updatedPhotos = [...photos, ...newPhotos];
            setPhotos(updatedPhotos);
            if (onChange) {
                onChange(updatedPhotos.map(p => p.file));
            }
        });

        e.target.value = '';
    };

    const removePhoto = (index) => {
        const updatedPhotos = photos.filter((_, i) => i !== index);
        setPhotos(updatedPhotos);
        if (onChange) {
            onChange(updatedPhotos.map(p => p.file));
        }
    };

    const canAddMore = photos.length < maxPhotos;

    useEffect(() => {
        if (resetToken !== undefined) {
            setPhotos([]);
        }
    }, [resetToken]);

    return (
        <div className="photo-upload-section">
            {canAddMore && (
                <div className="photo-upload-button">
                    <input
                        type="file"
                        accept="image/*"
                        multiple
                        capture="environment"
                        onChange={handleFileSelect}
                    />
                    <div className="photo-upload-icon">📷</div>
                    <div className="photo-upload-text">Добавить фото</div>
                    <div className="photo-upload-hint">(Если требуется)</div>
                </div>
            )}

            {photos.length > 0 && (
                <div className="photo-gallery">
                    {photos.map((photo, index) => (
                        <div key={index} className="photo-item">
                            <img src={photo.preview} alt={`Фото ${index + 1}`} />
                            <button
                                type="button"
                                className="photo-remove"
                                onClick={() => removePhoto(index)}
                            >
                                ×
                            </button>
                        </div>
                    ))}
                </div>
            )}

            <div className="photo-count">
                {photos.length} / {maxPhotos} фото
            </div>
        </div>
    );
}

export default PhotoGallery;
