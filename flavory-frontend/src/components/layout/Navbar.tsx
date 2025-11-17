import React, { Fragment } from 'react';
import { Link } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { useAppSelector } from '../../app/hooks';
import { Menu, Transition } from '@headlessui/react';
import { ChefHatIcon, UserIcon, LogOutIcon, SettingsIcon } from 'lucide-react';

const Navbar: React.FC = () => {
    const { logout } = useAuth0();
    const { user } = useAppSelector((state) => state.auth);

    const handleLogout = () => {
        logout({ logoutParams: { returnTo: window.location.origin } });
    };

    return (
        <nav className="bg-white shadow-sm border-b border-gray-200">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between h-16">
                    <div className="flex items-center">
                        <Link to="/" className="flex items-center space-x-2">
                            <div className="w-10 h-10 bg-primary-600 rounded-full flex items-center justify-center">
                                <ChefHatIcon className="w-6 h-6 text-white" />
                            </div>
                            <span className="text-xl font-bold text-gray-900">Flavory</span>
                        </Link>

                        <div className="hidden md:flex ml-10 space-x-8">
                            <Link
                                to="/dashboard"
                                className="text-gray-700 hover:text-primary-600 px-3 py-2 text-sm font-medium transition-colors"
                            >
                                Dashboard
                            </Link>
                            <Link
                                to="/dishes"
                                className="text-gray-700 hover:text-primary-600 px-3 py-2 text-sm font-medium transition-colors"
                            >
                                Dishes
                            </Link>
                            <Link
                                to="/orders"
                                className="text-gray-700 hover:text-primary-600 px-3 py-2 text-sm font-medium transition-colors"
                            >
                                Orders
                            </Link>
                        </div>
                    </div>

                    <div className="flex items-center">
                        <Menu as="div" className="relative">
                            <Menu.Button className="flex items-center space-x-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 rounded-lg p-2">
                                {user?.profileImageUrl ? (
                                    <img
                                        src={user.profileImageUrl}
                                        alt={user.fullName}
                                        className="w-8 h-8 rounded-full"
                                    />
                                ) : (
                                    <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center">
                                        <UserIcon className="w-5 h-5 text-primary-600" />
                                    </div>
                                )}
                                <span className="hidden md:block font-medium text-gray-700">
                  {user?.fullName}
                </span>
                            </Menu.Button>

                            <Transition
                                as={Fragment}
                                enter="transition ease-out duration-100"
                                enterFrom="transform opacity-0 scale-95"
                                enterTo="transform opacity-100 scale-100"
                                leave="transition ease-in duration-75"
                                leaveFrom="transform opacity-100 scale-100"
                                leaveTo="transform opacity-0 scale-95"
                            >
                                <Menu.Items className="absolute right-0 mt-2 w-48 origin-top-right bg-white rounded-lg shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
                                    <div className="py-1">
                                        <Menu.Item>
                                            {({ active }) => (
                                                <Link
                                                    to="/profile"
                                                    className={`${
                                                        active ? 'bg-gray-100' : ''
                                                    } flex items-center px-4 py-2 text-sm text-gray-700`}
                                                >
                                                    <UserIcon className="w-4 h-4 mr-3" />
                                                    Profile
                                                </Link>
                                            )}
                                        </Menu.Item>
                                        <Menu.Item>
                                            {({ active }) => (
                                                <Link
                                                    to="/settings"
                                                    className={`${
                                                        active ? 'bg-gray-100' : ''
                                                    } flex items-center px-4 py-2 text-sm text-gray-700`}
                                                >
                                                    <SettingsIcon className="w-4 h-4 mr-3" />
                                                    Settings
                                                </Link>
                                            )}
                                        </Menu.Item>
                                        <div className="border-t border-gray-100"></div>
                                        <Menu.Item>
                                            {({ active }) => (
                                                <button
                                                    onClick={handleLogout}
                                                    className={`${
                                                        active ? 'bg-gray-100' : ''
                                                    } flex items-center w-full px-4 py-2 text-sm text-red-600`}
                                                >
                                                    <LogOutIcon className="w-4 h-4 mr-3" />
                                                    Log out
                                                </button>
                                            )}
                                        </Menu.Item>
                                    </div>
                                </Menu.Items>
                            </Transition>
                        </Menu>
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;