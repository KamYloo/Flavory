import React from 'react';
import ReactDOM from 'react-dom/client';
import { Provider } from 'react-redux';
import { Auth0Provider } from '@auth0/auth0-react';
import { store } from './app/store';
import { auth0Config } from './services/auth0';
import App from './App';
import './index.css';

const root = ReactDOM.createRoot(
    document.getElementById('root') as HTMLElement
);

root.render(
    <React.StrictMode>
        <Auth0Provider
            domain={auth0Config.domain}
            clientId={auth0Config.clientId}
            authorizationParams={auth0Config.authorizationParams}
            cacheLocation={auth0Config.cacheLocation}
            useRefreshTokens={auth0Config.useRefreshTokens}
        >
            <Provider store={store}>
                <App />
            </Provider>
        </Auth0Provider>
    </React.StrictMode>
);