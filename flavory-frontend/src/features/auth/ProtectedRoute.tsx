import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { useAppSelector } from '../../app/hooks';
import Loading from '../../components/common/Loading';

interface ProtectedRouteProps {
    children: React.ReactNode;
    requiredRole?: string;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
                                                           children,
                                                           requiredRole
                                                       }) => {
    const { isAuthenticated, isLoading: auth0Loading } = useAuth0();
    const { user, isLoading: userLoading } = useAppSelector((state) => state.auth);
    const location = useLocation();

    if (auth0Loading || userLoading) {
        return <Loading />;
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (requiredRole && user?.role !== requiredRole) {
        return <Navigate to="/unauthorized" replace />;
    }

    return <>{children}</>;
};

export default ProtectedRoute;