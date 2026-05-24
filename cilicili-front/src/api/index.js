import request from '@/utils/request.js'

export function getUserById(id) {
    return request({
        url: `/api/users/getById/${id}`,
        method: 'POST'
    })
}

export function login(username, password) {
    // console.log(username, password)
    return request({
        url: `/api/users/login/`,
        method: 'POST',
        data: {
            username,
            password
        }
    })
}
export function register(username, phone, password) {
    console.log(username, phone, password)
    return request({
        url: `/api/users/register/`,
        method: 'POST',
        data: {
            username,
            phone,
            password,
        }
    })
}
export function currentUser() {
    return request({
        url: `/api/users/currentUser/`,
        method: 'POST'
    })
}
export function checkToken() {
    return request({
        url: `/api/users/check`,
        method: 'GET'
    })
}

export function uploadVideo(formData) {
    return request({
        url: `/api/videos/upload/`,
        method: 'POST',
        headers: { 'Content-Type': 'multipart/form-data' },
        data: formData
    })
}