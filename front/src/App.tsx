import { Route, Routes } from 'react-router-dom'
import SignUpPage from './pages/sign-up-page'
import './App.css'
import LoginPage from './pages/login-page'

function App() {

  return (
    <Routes>
      <Route path="/signup" element={<SignUpPage />} />
      <Route path="/login" element={<LoginPage />} />
    </Routes>
  )
}

export default App
