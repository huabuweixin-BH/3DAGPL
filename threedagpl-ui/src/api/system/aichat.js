import request from '@/utils/request'

// AI问答
export function chatAi(data) {
  return request({
    url: '/system/ai/chat/chat',
    method: 'post',
    data: data
  })
}

// 查询对话历史列表
export function listChatHistory(query) {
  return request({
    url: '/system/ai/chat/history/list',
    method: 'get',
    params: query
  })
}

// 查询用户的对话历史
export function getUserChatHistory(userId) {
  return request({
    url: '/system/ai/chat/history/user/' + userId,
    method: 'get'
  })
}

// 删除对话历史
export function delChatHistory(ids) {
  return request({
    url: '/system/ai/chat/history/' + ids,
    method: 'delete'
  })
}
