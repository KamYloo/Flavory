import React, { useState } from 'react';
import { useAppSelector } from '../../app/hooks';
import { useGetUserAddressesQuery, useUpdateUserMutation } from './userApi';
import { EditIcon, MapPinIcon, PhoneIcon, MailIcon } from 'lucide-react';
import { toast } from 'react-hot-toast';
import Loading from '../../components/common/Loading';

const Profile: React.FC = () => {
    const { user } = useAppSelector((state) => state.auth);
    const [isEditing, setIsEditing] = useState(false);

    const { data: addressesData, isLoading: addressesLoading } = useGetUserAddressesQuery(
        user?.id || 0,
        { skip: !user }
    );

    const [updateUser, { isLoading: updating }] = useUpdateUserMutation();

    const [formData, setFormData] = useState({
        firstName: user?.firstName || '',
        lastName: user?.lastName || '',
        phoneNumber: user?.phoneNumber || '',
        cookDescription: user?.cookDescription || '',
    });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!user) return;

        try {
            await updateUser({
                id: user.id,
                data: formData,
            }).unwrap();

            toast.success('Profile updated successfully!');
            setIsEditing(false);
        } catch (error) {
            toast.error('Failed to update profile');
        }
    };

    if (!user) {
        return <Loading />;
    }

    return (
        <div className="min-h-screen bg-gray-50 py-8">
            <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="card mb-6">
                    <div className="flex items-start justify-between">
                        <div className="flex items-center space-x-4">
                            {user.profileImageUrl ? (
                                <img
                                    src={user.profileImageUrl}
                                    alt={user.fullName}
                                    className="w-20 h-20 rounded-full"
                                />
                            ) : (
                                <div className="w-20 h-20 rounded-full bg-primary-100 flex items-center justify-center">
                  <span className="text-2xl font-bold text-primary-600">
                    {user.firstName[0]}{user.lastName[0]}
                  </span>
                                </div>
                            )}
                            <div>
                                <h1 className="text-2xl font-bold text-gray-900">{user.fullName}</h1>
                                <p className="text-gray-600">{user.email}</p>
                                <div className="flex items-center space-x-2 mt-2">
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                      user.role === 'COOK'
                          ? 'bg-purple-100 text-purple-800'
                          : 'bg-blue-100 text-blue-800'
                  }`}>
                    {user.role === 'COOK' ? 'Kucharz' : 'Klient'}
                  </span>
                                    {user.isVerified && (
                                        <span className="px-3 py-1 rounded-full text-sm font-medium bg-green-100 text-green-800">
                      Verified
                    </span>
                                    )}
                                </div>
                            </div>
                        </div>
                        <button
                            onClick={() => setIsEditing(!isEditing)}
                            className="btn-secondary"
                        >
                            <EditIcon className="w-4 h-4 mr-2 inline" />
                            {isEditing ? 'Cancel' : 'Edit'}
                        </button>
                    </div>
                </div>

                {isEditing ? (
                    <div className="card mb-6">
                        <h2 className="text-xl font-semibold text-gray-900 mb-4">
                            Edit profile
                        </h2>
                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div className="grid md:grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Name
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.firstName}
                                        onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                                        className="input-field"
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Last name
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.lastName}
                                        onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                                        className="input-field"
                                        required
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Phone number
                                </label>
                                <input
                                    type="tel"
                                    value={formData.phoneNumber}
                                    onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                                    className="input-field"
                                    placeholder="+48123456789"
                                />
                            </div>

                            {user.role === 'COOK' && (
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Description (visible to customers)
                                    </label>
                                    <textarea
                                        value={formData.cookDescription}
                                        onChange={(e) => setFormData({ ...formData, cookDescription: e.target.value })}
                                        className="input-field"
                                        rows={4}
                                        placeholder="Tell us about your passion for cooking..."
                                    />
                                </div>
                            )}

                            <div className="flex justify-end space-x-3">
                                <button
                                    type="button"
                                    onClick={() => setIsEditing(false)}
                                    className="btn-secondary"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={updating}
                                    className="btn-primary"
                                >
                                    {updating ? 'Saving...' : 'Save changes'}
                                </button>
                            </div>
                        </form>
                    </div>
                ) : (
                    <div className="card mb-6">
                        <h2 className="text-xl font-semibold text-gray-900 mb-4">
                            Information
                        </h2>
                        <div className="space-y-3">
                            <div className="flex items-center text-gray-600">
                                <MailIcon className="w-5 h-5 mr-3" />
                                <span>{user.email}</span>
                            </div>
                            {user.phoneNumber && (
                                <div className="flex items-center text-gray-600">
                                    <PhoneIcon className="w-5 h-5 mr-3" />
                                    <span>{user.phoneNumber}</span>
                                </div>
                            )}
                            {user.cookDescription && (
                                <div className="mt-4 p-4 bg-gray-50 rounded-lg">
                                    <p className="text-sm text-gray-600 mb-1">Description of the cook:</p>
                                    <p className="text-gray-900">{user.cookDescription}</p>
                                </div>
                            )}
                        </div>
                    </div>
                )}

                <div className="card">
                    <div className="flex items-center justify-between mb-4">
                        <h2 className="text-xl font-semibold text-gray-900">
                            Delivery addresses
                        </h2>
                        <button className="btn-primary text-sm">
                            Add address
                        </button>
                    </div>

                    {addressesLoading ? (
                        <div className="text-center py-8">
                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
                        </div>
                    ) : addressesData?.data && addressesData.data.length > 0 ? (
                        <div className="space-y-3">
                            {addressesData.data.map((address) => (
                                <div
                                    key={address.id}
                                    className="p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
                                >
                                    <div className="flex items-start justify-between">
                                        <div className="flex items-start">
                                            <MapPinIcon className="w-5 h-5 text-gray-400 mr-3 mt-1" />
                                            <div>
                                                <p className="font-medium text-gray-900">
                                                    {address.label || 'Address'}
                                                    {address.isDefault && (
                                                        <span className="ml-2 text-xs bg-primary-100 text-primary-800 px-2 py-1 rounded">
                              Default
                            </span>
                                                    )}
                                                </p>
                                                <p className="text-sm text-gray-600 mt-1">
                                                    {address.fullAddress}
                                                </p>
                                            </div>
                                        </div>
                                        <button className="text-gray-400 hover:text-gray-600">
                                            <EditIcon className="w-4 h-4" />
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="text-center py-8 text-gray-500">
                            <MapPinIcon className="w-12 h-12 mx-auto mb-3 text-gray-300" />
                            <p>You have no addresses added yet</p>
                            <button className="btn-primary mt-4">
                                Add first address
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Profile;