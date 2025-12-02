import { createContext, useContext, useState } from 'react';
import Toast from '../components/Toast';

const NotificationContext = createContext();

export function NotificationProvider({ children }) {
    const [notifications, setNotifications] = useState([]);

    const showNotification = (message, type = 'success', duration = 4000) => {
        const id = Date.now() + Math.random();
        const notification = { id, message, type, duration };
        
        setNotifications(prev => [...prev, notification]);
        
        return id;
    };

    const showError = (message, duration = 4000) => {
        return showNotification(message, 'error', duration);
    };

    const removeNotification = (id) => {
        setNotifications(prev => prev.filter(notif => notif.id !== id));
    };

    return (
        <NotificationContext.Provider value={{ showNotification, showError }}>
            {children}
            <div className="toast-container">
                {notifications.map(notif => (
                    <Toast
                        key={notif.id}
                        message={notif.message}
                        type={notif.type}
                        duration={notif.duration}
                        onClose={() => removeNotification(notif.id)}
                    />
                ))}
            </div>
        </NotificationContext.Provider>
    );
}

export function useNotification() {
    const context = useContext(NotificationContext);
    if (!context) {
        throw new Error('useNotification must be used within NotificationProvider');
    }
    return context;
}

