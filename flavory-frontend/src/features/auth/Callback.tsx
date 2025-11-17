import React, { useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch } from '../../app/hooks';
import { setUser, setError } from './authSlice';
import { useGetCurrentUserQuery } from '../user/userApi';
import Loading from '../../components/common/Loading';

const Callback: React.FC = () => {
    const { isAuthenticated, isLoading: auth0Loading, getAccessTokenSilently } = useAuth0();
    const navigate = useNavigate();
    const dispatch = useAppDispatch();

    const { data: userData, error, isLoading } = useGetCurrentUserQuery(undefined, {
        skip: !isAuthenticated,
    });

    useEffect(() => {
        if (userData?.data) {
            dispatch(setUser(userData.data));
            const returnTo = sessionStorage.getItem('returnTo') || '/dashboard';
            sessionStorage.removeItem('returnTo');
            navigate(returnTo, { replace: true });
        }
    }, [userData, dispatch, navigate]);

    useEffect(() => {
        if (error) {
            dispatch(setError('Failed to retrieve user data'));
            navigate('/login', { replace: true });
        }
    }, [error, dispatch, navigate]);

    if (auth0Loading || isLoading) {
        return <Loading message="logging in..." />;
    }

    return <Loading message="Redirection..." />;
};

export default Callback;