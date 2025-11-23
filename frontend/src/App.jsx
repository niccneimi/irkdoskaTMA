import { useState } from 'react';
import AdForm from './components/AdForm';
import { NotificationProvider } from './contexts/NotificationContext';
import './styles/variables.css';
import './styles/global.css';

function App() {
    const [activeTab, setActiveTab] = useState('paid');

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
                    </div>
                </div>

                <AdForm />

                <a href="#" className="footer-link">
                    Поддержка
                </a>
            </div>
        </NotificationProvider>
    );
}

export default App;
