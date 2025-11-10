import express from "express"
import { login, myProfile, signup, verifyOtp, verifyUser } from "../controller/authController.js"
import { isAuth } from "../middleware/isAuth.js"

const router = express.Router()
router.post("/signup", signup)
router.post("/login", login)
router.get("/verify/:token", verifyUser)
router.get("/verify-otp", verifyOtp)
router.get("/my-profile", isAuth, myProfile)

export default router