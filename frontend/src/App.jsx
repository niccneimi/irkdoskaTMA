import { useState, useEffect } from 'react';
import AdForm from './components/AdForm';
import Profile from './components/Profile';
import { NotificationProvider } from './contexts/NotificationContext';
import { retrieveRawInitData } from '@telegram-apps/sdk';
import './styles/variables.css';
import './styles/global.css';
import axios from 'axios';

function App() {
    const [activeTab, setActiveTab] = useState('paid');

    useEffect(() => {
        const dataRaw = retrieveRawInitData();
        axios.get('/api/login', {
            headers: {
                'Authorization': 'tma ' + dataRaw
            }
        }).catch(error => {
            console.error('Login error:', error);
        });
    }, []);

    return (
        <NotificationProvider>
            <div className="container">
                <div className="header">
                    <div className="header-tabs">
                        <div
                            className={`tab ${activeTab === 'free' ? 'active' : ''}`}
                            onClick={() => setActiveTab('free')}
                        >
                            Бесплатно
                        </div>
                        <div
                            className={`tab ${activeTab === 'paid' ? 'active' : ''}`}
                            onClick={() => setActiveTab('paid')}
                        >
                            Платно
                        </div>
                        <div
                            className={`tab ${activeTab === 'profile' ? 'active' : ''}`}
                            onClick={() => setActiveTab('profile')}
                        >
                            Профиль
                        </div>
                    </div>
                </div>

                {activeTab === 'profile' ? <Profile /> : <AdForm isPaid={activeTab === 'paid'} />}

                <a href="https://t.me/Horhi_NFT" className="footer-link">
                    Поддержка
                </a>
            </div>
        </NotificationProvider>
    );
}

export default App;
