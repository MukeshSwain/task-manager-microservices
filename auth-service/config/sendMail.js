import { createTransport } from "nodemailer"
const sendMail = async ({ email, subject, html })=>{
    const transport = createTransport({
        host: "smtp.gmail.com",
        port: 465,
        auth: {
            user: process.env.USER_EMAIL,
            pass: process.env.USER_PASSWORD
        }
    })

    await transport.sendMail({
      from: process.env.FROM_EMAIL,
      to: email,
      subject,
      html,
    });
}

export default sendMail