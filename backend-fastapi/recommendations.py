from pydantic import BaseModel
from sqlalchemy import create_engine, Column, Integer, String, ForeignKey, CLOB, Float
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# 오라클 데이터베이스 연결
oracle_connection_string = 'oracle+cx_oracle://{username}:{password}@{host}:{port}/{database}'
engine = create_engine(
    oracle_connection_string.format(
        username='FESTIVAL',
        password='1234',
        host='localhost',
        port='1521',
        database='xe'
))
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


class Member(Base):
    __tablename__ = "member"
    member_id = Column("MEMBER_ID", Integer, primary_key=True, autoincrement=True)
    email = Column("EMAIL", String(255), nullable=False, unique=True)
    mbti = Column("MBTI", String(255), nullable=True)
    name = Column("NAME", String(255), nullable=True)

class Bookmark(Base):
    __tablename__ = "bookmark"
    bookmark_id = Column("BOOKMARK_ID", Integer, primary_key=True, autoincrement=True)
    member_id = Column("MEMBER_ID", Integer, ForeignKey("member.MEMBER_ID"), nullable=False)
    board_id = Column("BOARD_ID", Integer, ForeignKey("board.BOARD_ID"), nullable=False)

class Board(Base):
    __tablename__ = "board"
    board_id = Column("BOARD_ID", Integer, primary_key=True, autoincrement=True)
    title = Column("TITLE", String(255), nullable=False)
    content = Column("CONTENT", CLOB, nullable=True)
    location = Column("LOCATION", String(255), nullable=True)
    latitude = Column("LATITUDE", Float, nullable=True)
    longitude = Column("LONGITUDE", Float, nullable=True)
    period = Column("PERIOD", String(255), nullable=True)
    contact = Column("CONTACT", String(255), nullable=True)

