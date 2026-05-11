create table teachers (
    id bigserial primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    full_name varchar(255) not null,
    role varchar(50) not null
);

create table classrooms (
    id bigserial primary key,
    teacher_id bigint not null references teachers(id),
    name varchar(255) not null,
    subject varchar(255) not null
);

create table virtual_rooms (
    id bigserial primary key,
    classroom_id bigint not null references classrooms(id),
    teacher_id bigint not null references teachers(id),
    room_code varchar(64) not null unique,
    room_pin varchar(16) not null,
    status varchar(32) not null,
    created_at timestamp with time zone not null
);

create table session_participants (
    id bigserial primary key,
    room_id bigint not null references virtual_rooms(id),
    role varchar(32) not null,
    display_name varchar(255) not null,
    phone varchar(32),
    joined_at timestamp with time zone not null
);
