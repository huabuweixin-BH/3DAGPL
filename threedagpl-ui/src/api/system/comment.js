import request from '@/utils/request'

// 查询文章评论列表
export function listComment(query) {
  return request({
    url: '/system/comment/list',
    method: 'get',
    params: query
  })
}

// 查询文章的评论列表（用于论坛页面）
export function listArticleComments(articleId) {
  return request({
    url: '/system/comment/article',
    method: 'get',
    params: { articleId }
  })
}

// 查询文章评论详细
export function getComment(id) {
  return request({
    url: '/system/comment/' + id,
    method: 'get'
  })
}

// 新增文章评论
export function addComment(data) {
  return request({
    url: '/system/comment',
    method: 'post',
    data: data
  })
}

// 修改文章评论
export function updateComment(data) {
  return request({
    url: '/system/comment',
    method: 'put',
    data: data
  })
}

// 删除文章评论
export function delComment(id) {
  return request({
    url: '/system/comment/' + id,
    method: 'delete'
  })
}

// 导出文章评论
export function exportComment(query) {
  return request({
    url: '/system/comment/export',
    method: 'post',
    params: query
  })
}
