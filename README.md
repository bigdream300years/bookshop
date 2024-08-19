 # 暑假学习项目一
此项目为暑假初学springboot创建的项目，主要用到技术有后端springboot、spring security、redis、mybatis-plus等，  
前端用vue+router+axios+pinia+echart
### 1.0版本
实现功能主要分为登录、注册、忘记密码找回、记住账户再次登录、通过邮箱验证码验证等功能  
### 2.0版本
网上书城系统，实现功能主要分为对书籍的增删查改和图片上传保存功能
***
## 一、登录模块 
### 1、登录流程
**功能：**
* 用户输入正确账户和密码点击登录跳转到后台页面  

**实现：**
* 前端填写符合格式的账户和密码后点击登录按钮将用户账户密码信息送到/api/auth/login接口
* /api/auth/login接口在config层SecurityConfig中的过滤器中，通过authenticationManager调用自己写在authorizeServiceImp中的loadUserByUsernamer方法对用户信息进行校验
* 在自己写的loadUserByUsernamer方法中拿到与前端输入用户名相同的账户信息，在authenticationManager中的AuthenticationProvider实现身份验证与前端传来用户名和密码进行比较
* 由验证成功和失败结果调用对应方法返回前端相应信息
* 如登录成功调用/api/user/me接口获取登录用户信息，并将用户信息存入pinia中实现页面跳转和拦截，只有pinia中存入用户信息才能访问后续页面，实现页面拦截功能

### 2、未登录对后端访问拦截流程

**功能：**
* 用户登录后才能访问后端其它接口  

**实现**
* 前端拦截通过路由守卫配置和pinia存储状态信息实现,在axios中给发送给后端请求中加上携带cookie存储登录后的用户数据供security系统验证
* 后端在config层的WebMvcConfig类中通过addInterceptors方法注入自己写的拦截器方法，对不在白名单的接口访问路径进行拦截（所以权限相关操作路径和其它业务操作路径最好区分便于拦截器拦截）
* 首先由spring security获取前端登录用户信息，接着在用户表中查找用户
* 在重写的prehandle方法中如果查到用户在这次会话session中存入名为用户信息
* 拦截功能主要靠spring security框架（拦截器中只起到存session作用，未写拦截逻辑），登录后
### 3、登陆时记住我流程
**功能：**
* 用户点击记住我按钮，下次不用登录自动进入用户页面  

**实现：**
* 前端用户账户密码信息送到/api/auth/login接口时remember值为真
* 在config层SecurityConfig中的过滤器中由remerberMe()方法创建token，token储存用户登录信息，将token存入到浏览器的cookie中，由rememberMeParameter("remember")指定是否实现记住我操作标志字段
* 由tokenRepository()指定token是如何存储的，tokenValiditySeconds()设置token过期时间即记住我的时间
* 在过滤器的参数中输入自己手动写的PersistentTokenRepository实例化函数，创建数据库源，将token存入mysql中，最后返回jdbcTokenRepository对象
* 当用户再次访问应用时，浏览器会自动发送包含令牌的Cookie给服务器，Spring Security框架会拦截请求，并尝试从Cookie或数据库中获取令牌。
* 如果令牌存在且有效（未过期），则根据令牌中的信息自动填充用户的认证信息，实现自动登录。
* 如果令牌不存在或无效，用户需要重新登录以获取新的令牌。
## 二、注册模块 
### 1、注册发送验证码流程
**功能：**
* 用户输入合法的邮箱地址后点击获取验证码按钮获取验证码

**实现：**
* 用户在前端填入经规则校验后的邮箱地址，点击获取验证码按钮后将邮箱地址送到/api/auth/valid-register-email接口
* 在controller层对应接口类的接口方法中调用service层中authorizeServiceImp类中sendValidateEmail方法进行验证码发送和验证操作
* 首先以用户sessionId（Spring Security自动处理session的创建和管理，用户每次登录时斗会生成个新sessionId，在退出登录时注销session）和email，hasAccount作为key值
* 经验证具备创建账户的条件后，生成验证码，由配置好的邮箱通过spring email依赖利用SimpleMailMessage类发送验证码给对应邮箱地址
* 若发送成功，在redis数据库存入key与验证码作为键值对供下一步。发送失败给出相应报错提示和错误返回，并在前端重置验证码冷却时间
### 2、注册账号流程
**功能：**
* 用户输入账号信息和验证码后生成账号记录在数据库中   

**实现：**
* 先在前端对表单进行验证，符合规则将信息送到/api/auth/register接口
* 在后端再对输入数据进行验证，在controller层对应接口类的接口方法中调用service层中authorizeServiceImp类中validateAndRegister方法进行注册账号操作
* 还是以用户sessionId和email，hasAccount作为key值，查找redis中是否有这个key值对应验证码（验证码时候存入）
* 与前端输入验证码进行比较，如果正确返回正确结果，错误返回其它提示信息
* 注册成功后跳转到登录界面
## 三、忘记密码模块 
### 1、忘记密码发送验证码流程
**功能：**
* 用户输入邮箱地址后获取用于重置密码的验证码  

**实现：**
* 用户输入邮箱地址后点击获取验证码按钮将信息送到/api/auth/valid-reset-email接口
* 再由在controller层对应接口类的接口方法中调用service层中authorizeServiceImp类的sendValidateEmail进行发送验证码和验证操作
* 与注册验证码不同是key中hasAccount的值为真，后续流程类似
### 2、开始重置密码流程
**功能：**
* 用户输入邮箱地址和验证码且验证了账号合法性后才可以重置密码   

**实现：**
* 用户输入邮箱地址与验证码后点击下一步按钮将验证码和邮箱地址发到/api/auth/start-reset接口
* 再由controller层对应接口类的接口方法中调用service层中authorizeServiceImp类的validateOnly进行验证是否有这个账号
* 还是先从redis取出key对应验证码进行验证，若验证通过往session中存入重置密码的邮箱地址进行下一步重置密码流程（相比注册账号一步到位重置密码把过程拆开用session暂存）
### 3、重置密码流程
**功能：**
* 用户输入新密码重置原来旧密码

**实现：**
* 用户输入新密码后点击确认重置密码按钮将信息发送到/api/auth/do-reset接口
* 再由controller层对应接口类的接口方法resetPassword进行处理先取得存在session中的邮箱地址
* 验证通过后将邮箱地址和新密码送入service层中authorizeServiceImp类的resetPassword方法中
* 在mapper层将数据库中邮箱为这个账号的密码更新为新密码
## 四、退出登录模块
### 1、退出登录流程
**功能：**
* 用户点击退出登录按钮登出  

**实现：**
* 前端点击退出登录后请求发送到/api/auth/logout接口
* 在config层过滤器中对登出成功请求做出回应
* 后端退出登录成功后在前端也跳回登录界面将pinia中状态清除
## 五、查阅书籍
### 1、显示书籍流程
**功能：**
* 在前端页面加载显示所有书籍，并可以进行分页查询操作

**实现：**
* 前端用onMounted钩子函数在页面渲染完成前调用getBookInfoList获取信息送到book/list/{current}/{pageSize}接口，当前页和每页容量信息包含在路径中传递
* 引入mybatis-plus依赖和在config层中配置Mybatis-Plus的分页插件，通过mybatis拦截器对mysql数据源数据提前进行操作
* 先在controller层对应接口类中创建由mybatis-plus提供的Page对象，值由前面参数提供，再创建由mybatis-plus提供的QueryWrapper对象写上查询条件
* 再调用bookService的selectPage方法将Page和QueryWrapper传入mapper层中bookmapper方法进行查询返回相应查询结果
* 前端根据后端返回信息进行相应操作
### 2、根据条件查询书籍流程
**功能：**
* 可通过条件进行书籍搜索操作  

**实现：**
* 前端在传参时多传入书名信息的json格式参数
* 在QueryWrapper对象里补上相应查询信息由继承了BaseMapper<Book>的bookmapper类自动调用方法进行查询
* 将查询结果返回前端
## 六、增加书籍功能
### 1、上传图片功能
**功能：**
* 可在前端调用接口将图片上传保存到指定位置  

**实现：**
* 前端有el-upload组件打开文件夹位置选择上传文件调用book/upload接口，发送请求和图片数据
* 在配置yml文件中指定文件上传地址
* 在接口对应类在首先由时间格式化工具类设置按时间保存的文件夹路径，接着打开这个文件夹如不存在就创建作为文件保存的目录
* 根据上传图片名作为保存这个图片的文件路径的后缀，然后创建这个文件保存到之前设置的文件夹里。
* 保存成功返回前端对应信息，发送错误抛出异常返回对应错误信息
### 2、新增书籍功能
**功能：**
* 在前端填好书籍信息后将书籍新增到商城中

**实现：**
* 前端书籍信息通过验证后将信息发送到book/add-one-book接口
* 再由controller层对应接口类调用bookService的addBookInfo进行书籍添加
* 在方法中创建书籍实体类，将前端传入的书籍信息填入对应属性中再调用bookMapper的insert方法插入到数据库中，返回插入影响的行数
* 在接口类里由插入影响的行数判断成功或失败，返回前端对应的信息
## 七、删除书籍功能
### 1、删除书籍流程
**功能：**
* 用户点击删除书籍按钮后删去书籍

**实现：**
* 前端将要删除书籍的id送到book/delete-one-book接口
* 再由controller层对应接口类调用bookService的deleteOneBook进行书籍删除
* 在方法中创建书籍实体类，将前端传入的书籍信息填入对应属性中再调用bookMapper的update方法将书籍信息更新为已删除，返回插入影响的行数
* 在接口类里由插入影响的行数判断成功或失败，返回前端对应的信息
## 八、修改书籍功能
### 1、删除书籍流程
**功能：**
* 用户点击修改书籍按钮后可修改书籍信息

**实现：**
* 前端将修改后书籍信息送到book/upd-one-book接口
* 再由controller层对应接口类调用bookService的updateOneBook进行书籍删除
* 在方法中创建书籍实体类，将前端传入的书籍信息填入对应属性中再调用bookMapper的update方法将书籍信息更新为已删除，返回插入影响的行数
* 在接口类里由插入影响的行数判断成功或失败，返回前端对应的信息