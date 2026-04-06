import { Navigate, Route, Routes } from 'react-router-dom'
import SignUpPage from './pages/sign-up-page'
import './App.css'
import LoginPage from './pages/login-page'
import ForgotPasswordPage from './pages/forgot-password-page'
import ResetPasswordPage from './pages/reset-password-page'
import AuthProvider from './providers/AuthProvider'
import Workspace from './layouts/Workspace'
import Channel from './pages/Channel'
import DirectMessage from './pages/DirectMessage'
import { useUser } from './store/auth'

function RootRedirect() {
  const user = useUser();
  return <Navigate to={user ? '/workspace/1/channel/일반' : '/login'} replace />;
}

function App() {

  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<RootRedirect />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/workspace/:workspaceId" element={<Workspace />}>
          <Route path="channel/:channelName" element={<Channel />} />
          <Route path="dm/:userId" element={<DirectMessage />} />
        </Route>
      </Routes>
    </AuthProvider>
  )
}

export default App
