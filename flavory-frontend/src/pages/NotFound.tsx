import React from 'react';
import { Link } from 'react-router-dom';
import { HomeIcon } from 'lucide-react';

const NotFound: React.FC = () => {
    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
            <div className="text-center">
                <h1 className="text-9xl font-bold text-primary-600">404</h1>
                <p className="text-2xl font-semibold text-gray-900 mt-4 mb-2">
                    Page not found
                </p>
                <p className="text-gray-600 mb-8">
                    Sorry, we can't find the page you're looking for.
                </p>
                <Link to="/" className="btn-primary inline-flex items-center space-x-2">
                    <HomeIcon className="w-5 h-5" />
                    <span>Return to homepage</span>
                </Link>
            </div>
        </div>
    );
};

export default NotFound;
