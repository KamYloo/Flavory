import React from 'react';
import { AlertCircle } from 'lucide-react';

interface ErrorMessageProps {
    message: string;
    onRetry?: () => void;
}

const ErrorMessage: React.FC<ErrorMessageProps> = ({ message, onRetry }) => {
    return (
        <div className="card bg-red-50 border border-red-200">
            <div className="flex items-start">
                <AlertCircle className="w-6 h-6 text-red-600 mr-3 flex-shrink-0 mt-0.5" />
                <div className="flex-1">
                    <h3 className="text-lg font-medium text-red-900 mb-1">
                        An error occurred
                    </h3>
                    <p className="text-red-700">{message}</p>
                    {onRetry && (
                        <button
                            onClick={onRetry}
                            className="mt-4 btn-primary bg-red-600 hover:bg-red-700"
                        >
                            Please try again
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ErrorMessage;