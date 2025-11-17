import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldAlertIcon, HomeIcon } from 'lucide-react';

const Unauthorized: React.FC = () => {
    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
            <div className="text-center">
                <div className="inline-flex items-center justify-center w-20 h-20 bg-red-100 rounded-full mb-6">
                    <ShieldAlertIcon className="w-10 h-10 text-red-600" />
                </div>
                <h1 className="text-4xl font-bold text-gray-900 mb-2">
                    Access denied
                </h1>
                <p className="text-xl text-gray-600 mb-8">
                    You do not have permission to view this page.
                </p>
                <Link to="/" className="btn-primary inline-flex items-center space-x-2">
                    <HomeIcon className="w-5 h-5" />
                    <span>Return to homepage</span>
                </Link>
            </div>
        </div>
    );
};

export default Unauthorized;
