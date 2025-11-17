import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { ChefHatIcon, ClockIcon, ShieldCheckIcon, HeartIcon } from 'lucide-react';

const Home: React.FC = () => {
    const { isAuthenticated, loginWithRedirect } = useAuth0();

    return (
        <div className="min-h-screen bg-white">
            <div className="relative bg-gradient-to-br from-primary-50 to-primary-100">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24">
                    <div className="text-center">
                        <h1 className="text-5xl md:text-6xl font-bold text-gray-900 mb-6">
                            Delicious homemade food
                            <br />
                            <span className="text-primary-600">from local chefs</span>
                        </h1>
                        <p className="text-xl text-gray-600 mb-8 max-w-2xl mx-auto">
                            Discover authentic flavors prepared by passionate cooks in your area
                        </p>
                        {isAuthenticated ? (
                            <Link
                                to="/dashboard"
                                className="btn-primary text-lg px-8 py-3 inline-block"
                            >
                                Go to Dashboard
                            </Link>
                        ) : (
                            <button
                                onClick={() => loginWithRedirect()}
                                className="btn-primary text-lg px-8 py-3"
                            >
                                Order now
                            </button>
                        )}
                    </div>
                </div>
            </div>

            <div className="py-24 bg-white">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="text-center mb-16">
                        <h2 className="text-3xl font-bold text-gray-900 mb-4">
                            Why Flavory?
                        </h2>
                        <p className="text-lg text-gray-600">
                            We connect lovers of homemade food with local chefs
                        </p>
                    </div>

                    <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
                        <div className="text-center">
                            <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
                                <ChefHatIcon className="w-8 h-8 text-primary-600" />
                            </div>
                            <h3 className="text-xl font-semibold text-gray-900 mb-2">
                                Local chefs
                            </h3>
                            <p className="text-gray-600">
                                Support passionate cooks from your neighborhood
                            </p>
                        </div>

                        <div className="text-center">
                            <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
                                <HeartIcon className="w-8 h-8 text-primary-600" />
                            </div>
                            <h3 className="text-xl font-semibold text-gray-900 mb-2">
                                Homemade flavors
                            </h3>
                            <p className="text-gray-600">
                                Authentic recipes and top-quality ingredients
                            </p>
                        </div>

                        <div className="text-center">
                            <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
                                <ClockIcon className="w-8 h-8 text-primary-600" />
                            </div>
                            <h3 className="text-xl font-semibold text-gray-900 mb-2">
                                Fast delivery
                            </h3>
                            <p className="text-gray-600">
                                Partnership with Glovo ensures lightning-fast delivery
                            </p>
                        </div>

                        <div className="text-center">
                            <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
                                <ShieldCheckIcon className="w-8 h-8 text-primary-600" />
                            </div>
                            <h3 className="text-xl font-semibold text-gray-900 mb-2">
                                Secure payments
                            </h3>
                            <p className="text-gray-600">
                                Your data is protected with the highest security standards
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            <div className="bg-primary-600 py-16">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
                    <h2 className="text-3xl font-bold text-white mb-4">
                        Ready for a delicious adventure?
                    </h2>
                    <p className="text-primary-100 text-lg mb-8">
                        Join thousands of satisfied Flavory users
                    </p>
                    {!isAuthenticated && (
                        <button
                            onClick={() => loginWithRedirect()}
                            className="bg-white text-primary-600 hover:bg-gray-100 font-medium py-3 px-8 rounded-lg transition-colors duration-200 text-lg"
                        >
                            Start now
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Home;
