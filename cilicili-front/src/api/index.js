import request from '@/utils/request.js'

export function getUserById(id) {
    return request({
        url: `/api/users/getById/${id}`,
        method: 'POST'
    })
}

export function getUserByName(name) {
    return request({
        url: `/api/users/getByName/${name}`,
        method: 'POST'
    })
}