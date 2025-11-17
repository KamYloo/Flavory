import React from 'react';

interface LoadingProps {
    message?: string;
}

const Loading: React.FC<LoadingProps> = ({ message = 'Loading...' }) => {
    return (
        <div className="min-h-screen flex flex-col items-center justify-center">
            <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-primary-600 mb-4"></div>
            <p className="text-gray-600 text-lg">{message}</p>
        </div>
    );
};

export default Loading;