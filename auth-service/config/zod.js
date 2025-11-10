import { z } from "zod"

const Roles = ["manager", "admin", "member"]

export const signupSchema = z.object({
  name: z
    .string()
    .trim()
    .min(3, { message: "Name must be at least 3 characters." })
    .max(100, { message: "Name must be at most 100 characters." }),

  email: z
    .string()
    .trim()
    .email({ message: "Invalid email address." })
    .max(254, { message: "Email is too long." }),
  password: z
    .string()
    .min(8, { message: "Password must be at least 8 characters." })
    .max(128, { message: "Password is too long." })
    .refine(
      (p) =>
        /[a-z]/.test(p) && // lowercase
        /[A-Z]/.test(p) && // uppercase
        /\d/.test(p) && // digit
        /[!@#$%^&*(),.?":{}|<>_\-\\[\]\/+=~`]/.test(p), // special
      {
        message:
          "Password must include uppercase, lowercase, a number, and a special character.",
      }
    ),
  role: z.enum(Roles, { message: "Invalid role provided." }),
});

export const loginSchema = z.object({
  email: z
    .string()
    .trim()
    .email({ message: "Invalid email address." })
    .max(254, { message: "Email is too long." }),
  password: z
    .string()
    .min(8, { message: "Password must be at least 8 characters." })
    .max(128, { message: "Password is too long." })
    .refine(
      (p) =>
        /[a-z]/.test(p) && // lowercase
        /[A-Z]/.test(p) && // uppercase
        /\d/.test(p) && // digit
        /[!@#$%^&*(),.?":{}|<>_\-\\[\]\/+=~`]/.test(p), // special
      {
        message:
          "Password must include uppercase, lowercase, a number, and a special character.",
      }
    )
});