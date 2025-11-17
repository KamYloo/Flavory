import React from 'react';
import { useAppSelector } from '../app/hooks';
import { Link } from 'react-router-dom';
import { PackageIcon, ChefHatIcon, UserIcon, TrendingUpIcon } from 'lucide-react';
import { UserRole } from '../types/user.types';

const Dashboard: React.FC = () => {
    const { user } = useAppSelector((state) => state.auth);

    if (!user) return null;

    const stats = [
        {
            name: 'Active orders',
            value: '3',
            icon: PackageIcon,
            change: '+2 since yesterday',
            changeType: 'increase',
        },
        {
            name: 'Favorite chefs',
            value: '12',
            icon: ChefHatIcon,
            change: '+3 this month',
            changeType: 'increase',
        },
        {
            name: 'Loyalty points',
            value: '450',
            icon: TrendingUpIcon,
            change: '+50 points',
            changeType: 'increase',
        },
    ];

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Header */}
            <div className="bg-white shadow-sm">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900">
                                Welcome, {user.firstName}! 👋
                            </h1>
                            <p className="text-gray-600 mt-1">
                                {user.role === UserRole.COOK
                                    ? 'Manage your dishes and orders'
                                    : 'Discover delicious homemade food in your area'}
                            </p>
                        </div>
                        <Link to="/profile" className="btn-primary">
                            <UserIcon className="w-5 h-5 mr-2 inline" />
                            My profile
                        </Link>
                    </div>
                </div>
            </div>

            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                    {stats.map((stat) => (
                        <div key={stat.name} className="card">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-sm text-gray-600 mb-1">{stat.name}</p>
                                    <p className="text-3xl font-bold text-gray-900">{stat.value}</p>
                                    <p className="text-sm text-green-600 mt-2">{stat.change}</p>
                                </div>
                                <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center">
                                    <stat.icon className="w-6 h-6 text-primary-600" />
                                </div>
                            </div>
                        </div>
                    ))}
                </div>

                <div className="grid md:grid-cols-2 gap-6">
                    <div className="card">
                        <h2 className="text-xl font-semibold text-gray-900 mb-4">
                            Quick actions
                        </h2>
                        <div className="space-y-3">
                            <Link
                                to="/dishes"
                                className="block p-4 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors"
                            >
                                <div className="flex items-center">
                                    <ChefHatIcon className="w-5 h-5 text-primary-600 mr-3" />
                                    <span className="font-medium">Browse dishes</span>
                                </div>
                            </Link>
                            <Link
                                to="/orders"
                                className="block p-4 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors"
                            >
                                <div className="flex items-center">
                                    <PackageIcon className="w-5 h-5 text-primary-600 mr-3" />
                                    <span className="font-medium">My orders</span>
                                </div>
                            </Link>
                        </div>
                    </div>

                    <div className="card">
                        <h2 className="text-xl font-semibold text-gray-900 mb-4">
                            Recent activity
                        </h2>
                        <div className="space-y-4">
                            <div className="flex items-start">
                                <div className="w-2 h-2 bg-green-500 rounded-full mt-2 mr-3"></div>
                                <div>
                                    <p className="text-sm font-medium text-gray-900">
                                        Order #1234 delivered
                                    </p>
                                    <p className="text-xs text-gray-500">2 hours ago</p>
                                </div>
                            </div>
                            <div className="flex items-start">
                                <div className="w-2 h-2 bg-blue-500 rounded-full mt-2 mr-3"></div>
                                <div>
                                    <p className="text-sm font-medium text-gray-900">
                                        New dish from Chef Jan
                                    </p>
                                    <p className="text-xs text-gray-500">5 hours ago</p>
                                </div>
                            </div>
                            <div className="flex items-start">
                                <div className="w-2 h-2 bg-yellow-500 rounded-full mt-2 mr-3"></div>
                                <div>
                                    <p className="text-sm font-medium text-gray-900">
                                        Received 50 loyalty points
                                    </p>
                                    <p className="text-xs text-gray-500">1 day ago</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
