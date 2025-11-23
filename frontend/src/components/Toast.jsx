import { useEffect } from 'react';
import '../styles/toast.css';

function Toast({ message, type = 'success', onClose, duration = 4000 }) {
    useEffect(() => {
        const timer = setTimeout(() => {
            onClose();
        }, duration);

        return () => clearTimeout(timer);
    }, [duration, onClose]);

    return (
        <div className={`toast toast-${type}`}>
            <div className="toast-content">
                <span className="toast-icon">
                    {type === 'success' && '✓'}
                    {type === 'error' && '✕'}
                    {type === 'info' && 'ℹ'}
                    {type === 'warning' && '⚠'}
                </span>
                <span className="toast-message">{message}</span>
            </div>
            <button className="toast-close" onClick={onClose} aria-label="Закрыть">
                ×
            </button>
        </div>
    );
}

export default Toast;

