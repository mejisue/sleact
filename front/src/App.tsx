import { Route, Routes } from 'react-router-dom'
import SignUpPage from './pages/sign-up-page'
import './App.css'

function App() {

  return (
    <Routes>
      <Route path="/signup" element={<SignUpPage />} />
    </Routes>
  )
}

export default App
