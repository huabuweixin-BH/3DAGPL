import request from '@/utils/request'

// 查询文章的评论列表
export function listComments(articleId) {
  return request({
    url: '/system/comment/list',
    method: 'get',
    params: { articleId }
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

// 删除文章评论
export function delComment(ids) {
  return request({
    url: '/system/comment/' + ids,
    method: 'delete'
  })
}
