import React, { useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ChefHatIcon } from 'lucide-react';

const Login: React.FC = () => {
    const { loginWithRedirect, isAuthenticated, isLoading } = useAuth0();
    const navigate = useNavigate();
    const location = useLocation();

    const from = (location.state as any)?.from?.pathname || '/dashboard';

    useEffect(() => {
        if (isAuthenticated) {
            navigate(from, { replace: true });
        }
    }, [isAuthenticated, navigate, from]);

    const handleLogin = () => {
        loginWithRedirect({
            appState: { returnTo: from },
        });
    };

    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-primary-50 to-primary-100 flex items-center justify-center px-4">
            <div className="max-w-md w-full">
                <div className="text-center mb-8">
                    <div className="inline-flex items-center justify-center w-16 h-16 bg-primary-600 rounded-full mb-4">
                        <ChefHatIcon className="w-8 h-8 text-white" />
                    </div>
                    <h1 className="text-4xl font-bold text-gray-900 mb-2">Flavory</h1>
                    <p className="text-gray-600">
                        Delicious homemade food from local chefs
                    </p>
                </div>

                <div className="card">
                    <h2 className="text-2xl font-semibold text-center mb-6">
                        Log in
                    </h2>

                    <button
                        onClick={handleLogin}
                        className="w-full btn-primary py-3 text-lg"
                    >
                        Log in via Auth0
                    </button>

                    <p className="text-center text-sm text-gray-600 mt-6">
                        You don't have an account?{' '}
                        <button
                            onClick={handleLogin}
                            className="text-primary-600 hover:text-primary-700 font-medium"
                        >
                            Register
                        </button>
                    </p>
                </div>

                <div className="mt-8 text-center text-sm text-gray-600">
                    <p>By continuing you accept our</p>
                    <div className="space-x-4 mt-2">
                        <a href="/terms" className="text-primary-600 hover:text-primary-700">
                            Terms of Use
                        </a>
                        <span>•</span>
                        <a href="/privacy" className="text-primary-600 hover:text-primary-700">
                            Privacy Policy
                        </a>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;