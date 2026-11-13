
if not exists (select name from sys.databases where name = n'articles')
begin
    create database articles;
end
go

use articles;
go

if not exists (select 1 from sys.tables where name = n'category')
begin
    create table category (
        idcategory  int           identity(1,1) not null,
        name        nvarchar(100) not null,
        description nvarchar(500) null,

        constraint pk_category primary key (idcategory)
    );
end
go

if not exists (select 1 from sys.tables where name = n'author')
begin
    create table author (
        idauthor int           identity(1,1) not null,
        name     nvarchar(100) not null,
        bio      nvarchar(max) null,
        email    nvarchar(150) null,

        constraint pk_author primary key (idauthor)
    );
end
go

if not exists (select 1 from sys.columns where object_id = object_id(n'author') and name = n'bio')
begin
    alter table author add bio nvarchar(max) null;
end
go

if not exists (select 1 from sys.tables where name = n'article')
begin
    create table article (
        idarticle     int            identity(1,1) not null,
        title         nvarchar(255)  not null,
        link          nvarchar(500)  null,
        description   nvarchar(max)  null,
        picturepath   nvarchar(500)  null,
        publisheddate nvarchar(30)   null,
        idcategory    int            null,
        idauthor      int            null,

        constraint pk_article    primary key (idarticle),
        constraint fk_art_cat    foreign key (idcategory) references category (idcategory),
        constraint fk_art_author foreign key (idauthor)   references author   (idauthor)
    );
end
go

if not exists (select 1 from sys.tables where name = n'users')
begin
    create table users (
        iduser   int           identity(1,1) not null,
        username nvarchar(50)  not null,
        password nvarchar(255) not null,
        role     nvarchar(20)  not null constraint df_users_role default 'user',

        constraint pk_users       primary key (iduser),
        constraint uq_users_name  unique      (username),
        constraint ck_users_role  check       (role in ('administrator', 'user'))
    );
end
go

drop procedure if exists createarticle;
go
create procedure createarticle
    @title         nvarchar(255),
    @link          nvarchar(500),
    @description   nvarchar(max),
    @picturepath   nvarchar(500),
    @publisheddate nvarchar(30),
    @idarticle     int output,
    @idcategory    int = null,
    @idauthor      int = null
as
begin
    set nocount on;

    insert into article (title, link, description, picturepath, publisheddate, idcategory, idauthor)
    values (@title, @link, @description, @picturepath, @publisheddate, @idcategory, @idauthor);

    set @idarticle = scope_identity();
end
go

drop procedure if exists updatearticle;
go
create procedure updatearticle
    @title         nvarchar(255),
    @link          nvarchar(500),
    @description   nvarchar(max),
    @picturepath   nvarchar(500),
    @publisheddate nvarchar(30),
    @idarticle     int
as
begin
    set nocount on;

    update article
    set title         = @title,
        link          = @link,
        description   = @description,
        picturepath   = @picturepath,
        publisheddate = @publisheddate
    where idarticle = @idarticle;
end
go

drop procedure if exists deletearticle;
go
create procedure deletearticle
    @idarticle int
as
begin
    set nocount on;

    delete from article
    where idarticle = @idarticle;
end
go

drop procedure if exists selectarticle;
go
create procedure selectarticle
    @idarticle int
as
begin
    set nocount on;

    select idarticle,
           title,
           link,
           description,
           picturepath,
           publisheddate,
           idcategory,
           idauthor
    from   article
    where  idarticle = @idarticle;
end
go

drop procedure if exists selectarticles;
go
create procedure selectarticles
as
begin
    set nocount on;

    select idarticle,
           title,
           link,
           description,
           picturepath,
           publisheddate,
           idcategory,
           idauthor
    from   article
    order by idarticle;
end
go

drop procedure if exists createuser;
go
create procedure createuser
    @username nvarchar(50),
    @password nvarchar(255),
    @role     nvarchar(20)
as
begin
    set nocount on;

    insert into users (username, password, role)
    values (@username, @password, @role);
end
go

drop procedure if exists selectuserbycredentials;
go
create procedure selectuserbycredentials
    @username nvarchar(50),
    @password nvarchar(255)
as
begin
    set nocount on;

    select iduser,
           username,
           password,
           role
    from   users
    where  username = @username
      and  password = @password;
end
go

drop procedure if exists selectallusers;
go
create procedure selectallusers
as
begin
    set nocount on;

    select iduser,
           username,
           password,
           role
    from   users
    order by iduser;
end
go

drop procedure if exists checkuserexists;
go
create procedure checkuserexists
    @username   nvarchar(50),
    @userexists bit output
as
begin
    set nocount on;

    if exists (select 1 from users where username = @username)
        set @userexists = 1;
    else
        set @userexists = 0;
end
go

drop procedure if exists createadminuser;
go
create procedure createadminuser
as
begin
    set nocount on;

    if not exists (select 1 from users where username = 'admin')
    begin
        insert into users (username, password, role)
        values ('admin', 'admin', 'administrator');
    end
end
go

exec createadminuser;
go

drop procedure if exists createcategory;
go
create procedure createcategory
    @name        nvarchar(100),
    @description nvarchar(500),
    @idcategory  int output
as
begin
    set nocount on;

    insert into category (name, description)
    values (@name, @description);

    set @idcategory = scope_identity();
end
go

drop procedure if exists updatecategory;
go
create procedure updatecategory
    @name        nvarchar(100),
    @description nvarchar(500),
    @idcategory  int
as
begin
    set nocount on;

    update category
    set name        = @name,
        description = @description
    where idcategory = @idcategory;
end
go

drop procedure if exists deletecategory;
go
create procedure deletecategory
    @idcategory int
as
begin
    set nocount on;

    delete from category
    where idcategory = @idcategory;
end
go

drop procedure if exists selectcategory;
go
create procedure selectcategory
    @idcategory int
as
begin
    set nocount on;

    select idcategory,
           name,
           description
    from   category
    where  idcategory = @idcategory;
end
go

drop procedure if exists selectallcategories;
go
create procedure selectallcategories
as
begin
    set nocount on;

    select idcategory,
           name,
           description
    from   category
    order by name;
end
go

drop procedure if exists createauthor;
go
create procedure createauthor
    @name     nvarchar(100),
    @bio      nvarchar(max),
    @email    nvarchar(150),
    @idauthor int output
as
begin
    set nocount on;

    insert into author (name, bio, email)
    values (@name, @bio, @email);

    set @idauthor = scope_identity();
end
go

drop procedure if exists updateauthor;
go
create procedure updateauthor
    @name     nvarchar(100),
    @bio      nvarchar(max),
    @email    nvarchar(150),
    @idauthor int
as
begin
    set nocount on;

    update author
    set name  = @name,
        bio   = @bio,
        email = @email
    where idauthor = @idauthor;
end
go

drop procedure if exists deleteauthor;
go
create procedure deleteauthor
    @idauthor int
as
begin
    set nocount on;

    delete from author
    where idauthor = @idauthor;
end
go

drop procedure if exists selectauthor;
go
create procedure selectauthor
    @idauthor int
as
begin
    set nocount on;

    select idauthor,
           name,
           bio,
           email
    from   author
    where  idauthor = @idauthor;
end
go

drop procedure if exists selectallauthors;
go
create procedure selectallauthors
as
begin
    set nocount on;

    select idauthor,
           name,
           bio,
           email
    from   author
    order by name;
end
go

drop procedure if exists deleteallarticles;
go
create procedure deleteallarticles
as
begin
    set nocount on;

    delete from article;
end
go

drop procedure if exists assignarticleauthor;
go
create procedure assignarticleauthor
    @idarticle int,
    @idauthor  int
as
begin
    set nocount on;

    update article
    set    idauthor = @idauthor
    where  idarticle = @idarticle;
end
go