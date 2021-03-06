--19장 사용자 추가시(createworkspace) 오라클데스크탑 X 대신
--웹프로그램을 사용 (http://127.0.0.1:9000/aoex/f?p=4950)
--15장 pk생성시 자동으로 2가 생성 not null(빈값방지),unique(no중복)
--제약조건 constraint이 자동생성, index(테이블)도 자동생성(검색시 중요)
--erd로 게시판 테이블-[댓글,첨부파일]foreign key외래키 부자관계생성
--14장 트랜잭션                     
--스프링단에서 트랜잭션을 사용 @transactionl 인터페이스를 사용
--commit과follback;(dml문:insert,update,delete)
--rollback는 제일 마지막 커밋된 상태로 돌아감
--12테이블 구조생성(create)변경(alter)삭제(drop)
--ERD관계형 다이어그램으로 물리적 테이블 생성
DROP TABLE tbl_member_del;
CREATE TABLE TBL_MEMBER_DEL
(
USER_ID VARCHAR(50) PRIMARY KEY
,USER_PW VARCHAR(255)
,USER_NAME VARCHAR(255)
,EMAIL VARCHAR(255)
,POINT NUMBER(11)
,ENABLED NUMBER(1)
,LEVELS VARCHAR(255)
,REG_DATE TIMESTAMP
,UPDATE_DATE TIMESTAMP
);
--ALTER 테이블로 필드명 변경
DESC tbl_member_del;
ALTER TABLE tbl_member_del RENAME COLUMN email TO user_email;
--DEPT테이블의 deptno 숫자2자리때문에 에러->4자리 크기로 변경
DESC dept;--작은 자리에서 큰자리로 변경 문제없음 
ALTER TABLE dept MODIFY(deptno NUMBER(4));
--11서브쿼리
--단일행서브쿼리 필드값을 비교할때, 비교하는 값인 단일한 
--다중행서브쿼리 테이블값을 select쿼리로 생성 (레코드형식)
--10 테이블 조인 2개테이블 연결해서 결과를 구하는 예약어
--댓글 개수 구할때
--카티시안프러덕트 조인(합집합-게시물10,댓글100=110)
--(인너)조인 (교집합)을 제일 많이 사용
--아래 조인방식이 오라클 방식
SELECT dept.dname, emp.* FROM emp, dept 
WHERE emp.deptno = dept.deptno
AND emp.ename ='SCOTT';
--표준 쿼리(ansi)방식(아래)inner키워드 디폴트 값
SELECT d.dname, e.* FROM emp e INNER JOIN dept d 
ON e.deptno = d.deptno
WHERE e.ename = 'SCOTT';
--조인과 그룹을 이용해서 댓글카운터 출력하는 게시판 리스트 만들기
SELECT bod.bno,title||'['||count(*)||']'
,writer,bod.reg_date,view_count
FROM tbl_board BOD
INNER JOIN tbl_reply REP ON bod.bno=rep.bno
GROUP BY bod.bno, title, writer, bod.reg_date, view_count
ORDER BY bod.bno;
--9장 패스(레포트용 함수사용)
--8장 함수(count,upper,lower,to_char,round...)그룹함수
--having은 group by의 조건문을 적습니다.
--부서별 평균 연봉이 2000 이상 부서의 번호와 부서별평균급여
SELECT deptno, round(AVG(sal)) FROM emp 
--where avg(sal) >=2000 --검색조건
GROUP BY deptno
HAVING AVG (sal) >= 2000; --그룹조건
--부서별 연봉의 합계를 구해서 제일 급여가 많이 지출되는 부서
--자바 코딩에서는 소문자로 통일 select
--db 셋팅에서 대소문자구분해서 사용할지, 구분하지 않을지 셋팅
SELECT R.* FROM (
SELECT deptno, Sum(sal) AS dept_sal
FROM emp GROUP BY deptno
) R ORDER BY dept_sal DESC;
SELECT deptno, SUM(sal) FROM emp
GROUP BY deptno
ORDER BY SUM(sal) DESC;
--라운드함수(반올림)소수점기준. round(10.05,2)소수점2번째 반올림
SELECT ename, round( sal,-3) FROM emp; 
SELECT SUM(sal) FROM emp;--1개의 레코드만 그룹함수
SELECT round(AVG(sal)) FROM emp;--평균 1개의 레코드로 출력
SELECT COUNT(*) FROM emp WHERE sal >=
(SELECT round(AVG(sal)) FROM emp);
-- 위 쿼리는 사원중에 평균연봉보다 많이 받는 사람의 숫자.
--위 AVG함수를 where조건에 사용못할때 서브쿼리를 이용합니다.
SELECT MAX(sal)
, MAX(sal)
, MIN(sal)-MIN(sal) AS "대표와사원의연봉차"
FROM emp;
--DDL문 (create; alter;),DML문 , DCL문(commit; rollcabk;)
--DML문 (Data Manufcture Language) insert,update,delete
--insert문:테이블 새로운 레코드(row)를 추가
CREATE TABLE dept02 AS SELECT * FROM dept WHERE 1=0;
--위 쿼리는 테이블을 복제하는 명령
--위처럼 춰리를 실행 dept테이블과 구조와 내용이 똑같은 테이블생성
--where 조건이 붙으면, 구조는 같으나 내용은 빈 테이블 생성
INSERT INTO dept02 (
--필드명
deptno, dname, loc
)VALUES (
10, '개발부서', '천안'
--바인딩값
);
COMMIT; --데이터베이스쿼리 직접 입력한 결과는 반드시 커밋을 해줘야지만
--실제 저장이 됩니다. 커밋x 여기만 보이고, 다른곳은x
SELECT * FROM dept02 ORDER BY deptno;
--DELETE 는 레코드 한줄을 지우는 명령
DELETE FROM dept02;
DELETE FROM dept02 WHERE deptno >= 0;
--DROP table 테이블명은 테이블자체를 물리적으로 없애는 명령 
DROP TABLE dept02; --드롭 테이블 커밋없이 바로 적용딤
CREATE TABLE emp01 AS SELECT * FROM emp; --테이블복제명령
SELECT * FROM emp01;
--UPDATE 테이블명 set 필드명 = '바꿀값' where empno= '특정ID'
UPDATE emp01 SET ename = '홍길동' WHERE empno = 7839;
ROLLBACK;--DCL문 마지막 커밋 바로전까지 되돌립니다.
UPDATE emp01 SET sal = sal *1.1;--모든 직원 연봉을 10%인상
UPDATE emp01 SET hiredate = sysdate;
--머시 표준쿼리 건너뜀

