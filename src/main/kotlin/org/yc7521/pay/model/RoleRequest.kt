/*
 * TODO:
 * 
 * user info need a field `nickname`
 * 
 * need a request model
 *  - id, Long, pri key
 *  - userInfo, UserInfo, the applicant of request
 *  - to, enums.UserType, the permission requested
 *  - create, LocalDateTime (create_time in sql), create time
 *  - finish, LocalDateTime (finish_time in sql), finish time
 *  - approver, UserInfo, the approver of request
 *  - state, enum RoleReqState{ unprocessed, permit, reject }, the state of request
 */