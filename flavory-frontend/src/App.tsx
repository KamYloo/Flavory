import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { Toaster } from 'react-hot-toast';
import { useAppDispatch } from './app/hooks';
import { setUser, setLoading, clearUser } from './features/auth/authSlice';
import { useGetCurrentUserQuery, setAccessTokenGetter } from './features/user/userApi';

// Components
import Navbar from './components/layout/Navbar';
import Loading from './components/common/Loading';
import ProtectedRoute from './features/auth/ProtectedRoute';

// Pages
import Home from './pages/Home';
import Login from './features/auth/Login';
import Callback from './features/auth/Callback';
import Dashboard from './pages/Dashboard';
import Profile from './features/user/Profile';
import NotFound from './pages/NotFound';
import Unauthorized from './pages/Unauthorized';

const App: React.FC = () => {
    const { isLoading: auth0Loading, isAuthenticated, getAccessTokenSilently } = useAuth0();
    const dispatch = useAppDispatch();

    useEffect(() => {
        setAccessTokenGetter(getAccessTokenSilently);
    }, [getAccessTokenSilently]);

    const { data: userData, isLoading: userLoading } = useGetCurrentUserQuery(undefined, {
        skip: !isAuthenticated,
    });

    useEffect(() => {
        if (userData?.data) {
            dispatch(setUser(userData.data));
        } else if (!isAuthenticated && !auth0Loading) {
            dispatch(clearUser());
        }
    }, [userData, isAuthenticated, auth0Loading, dispatch]);

    useEffect(() => {
        dispatch(setLoading(auth0Loading || userLoading));
    }, [auth0Loading, userLoading, dispatch]);

    if (auth0Loading) {
        return <Loading message="Initialization..." />;
    }

    return (
        <Router>
            <div className="min-h-screen bg-gray-50">
                <Toaster
                    position="top-right"
                    toastOptions={{
                        duration: 4000,
                        style: {
                            background: '#363636',
                            color: '#fff',
                        },
                        success: {
                            duration: 3000,
                            iconTheme: {
                                primary: '#10b981',
                                secondary: '#fff',
                            },
                        },
                        error: {
                            duration: 4000,
                            iconTheme: {
                                primary: '#ef4444',
                                secondary: '#fff',
                            },
                        },
                    }}
                />

                {isAuthenticated && <Navbar />}

                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/callback" element={<Callback />} />

                    <Route
                        path="/dashboard"
                        element={
                            <ProtectedRoute>
                                <Dashboard />
                            </ProtectedRoute>
                        }
                    />

                    <Route
                        path="/profile"
                        element={
                            <ProtectedRoute>
                                <Profile />
                            </ProtectedRoute>
                        }
                    />

                    <Route path="/unauthorized" element={<Unauthorized />} />
                    <Route path="/404" element={<NotFound />} />
                    <Route path="*" element={<Navigate to="/404" replace />} />
                </Routes>
            </div>
        </Router>
    );
};

export default App;