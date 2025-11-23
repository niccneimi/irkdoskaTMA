import { useState } from 'react';
import AdForm from './components/AdForm';
import './styles/variables.css';
import './styles/global.css';

function App() {
    const [activeTab, setActiveTab] = useState('paid');

    return (
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
    );
}

export default App;
