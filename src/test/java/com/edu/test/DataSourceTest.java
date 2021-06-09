package com.edu.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.edu.service.IF_MemberService;
import com.edu.vo.MemberVO;
import com.edu.vo.PageVO;

/**
 * 이 클래스는 오라클과 연동해서 CRUD를 테스트하는 클래스
 * 
 * @author 전민아
 *
 */
//RunWith 인터페이스는 현재 클래스가 JUnit실행클래스라고 명시
@RunWith(SpringJUnit4ClassRunner.class)
//경로에서**(모든폴더명시),*(모든파일명을명시)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/spring/**/*.xml" })

@WebAppConfiguration
public class DataSourceTest {
	// 디버그용 로그 객체변수생성
	private Logger logger = Logger.getLogger(DataSourceTest.class);
	//dataSource 객체는 데이터베이스객체를 pool로 저장해서 사용할때  DataSource클래스를 사용(아래)
	@Inject //인젝트는 스프링에서 객체를 만드는 방법
	private DataSource dataSource;//Inject로 객체를 만들면 메모리 관리를 스프링이 대신해줌
	//Inject 자바8부터 지원, 이전 자바7에서@Autowired로 객체를 만듬
    @Inject//MemberService서비스를 주입받아서 객체를 사용(아래)
    private IF_MemberService memberService;
	
    @Test 
    public void deleteMember() throws Exception{
    	memberService.deleteMember("user_del");
    	selectMember();
    }
    @Test
    public void insertMember() throws Exception {
    	MemberVO memberVO = new MemberVO();
    	//insert쿼리에 저장할 객체
    	memberVO.setUser_id("user_del");
    	memberVO.setUser_pw("1234");//스프릥 시큐리티5버전으로  암호와로 처리예정
    	memberVO.setEmail("user@test.com");
    	memberVO.setPoint(10);
    	memberVO.setEnabled(true);
    	memberVO.setLevels("ROLE_USER");
    	memberVO.setUser_name("삭제할사용자");
    	memberService.insertMember(memberVO);
    	selectMember();
    }
	//스프링 코딩 시작 순서
	@Test
	public void selectMember() throws Exception {
		//회원관리 테이블에서 더미로 입력한 100개의 레코드를 출력 메서드 테스트->회원관리 목록이 출력
		//검색기능, 페이징기능 여기서 구현.1페이지에 10명씩 나오게 변경
		//현재 몇페이지, 검색어 임시저장 공간 ->DB에 페이징조건문,검색조건문
		//변수를 2-3이상은 바로 String변수로 처리하지않고,VO만들어 사용
		//PageVO.java클래스 만들어서 페이징처리변수와 검색어 변수 선언, GET/set생성
		//PageVO만들기전 SQL쿼리로 가상으로 페이지를 한번 구현해 보면서, 필요한 변수 만들어야합니다.
		//pageVO객체를 만들어서 가상으로 초기 값을 입력합니다(아래)
		PageVO pageVO = new PageVO();
		
		pageVO.setPage(1);//기본값으로 1페이지를 입력
		pageVO.setPerPageNum(10);//UI하단의 페이지 개수
		pageVO.setQueryPerPageNum(10);//쿼리에서 페이지당 개수
		pageVO.setTotalCount(memberService.countMember());//테스트하는거
		
		pageVO.setSearch_type("user_id");//검색타입all,user_id,user_name
		pageVO.setSearch_keyword("user_del");//검색어
		//위 setTotalCount위치가 다른 설정보다 상단이면, 에러발생 이유는,calcPage()가 실행되는데,위3가지 변수값이 저장되있어야지 계산메서드가 정상작동되기때문입니다
		//위토탈카운트변수값은 startPage,endPage계산에 필수입니다.
		//매퍼쿼리_DAO클래스_Service클래스_JUnit(나중엔 컨트롤러에서 작업)이제는 역순으로 작업
		//더 진행하기 전에 현재 pageVO객체에는 어떤 값이 들어있는 확인하고 사용하겠습니다(아래)
		logger.info("디버그: "+pageVO.toString());
		List<MemberVO> listMember = memberService.selectMember(pageVO);
		listMember.toString();
	}
	
	@Test
	public void oldQueryTest() throws Exception {
		//스프링빈을 사용하지 않을때 예전 방식:코딩테스트에서는 스프링 설정을  x,직접DB아이디/암호 입력
		Connection connection = null;
		connection = 
	DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/XE","XE","apmsetup");
		logger.debug("데이터베이스 직접 접속이 성공 하였습니다. DB종류는 "+ connection.getMetaData().getDatabaseProductName());;
		//직접쿼리를 날립니다.날리기전 쿼리문장 객체생성 statement
		Statement stmt = connection.createStatement();
		//위 쿼리문장객체를 만드는 이유? 보안(SQL인젝션공격을 방지)
		//stmt객체가 없으면, 개발자가 SQL인젝션 방지코딩을 넣어야 합니다.
		//insert쿼리 문장 만듬(아래)
		//예전 방식으로 더미데이터
		/*
		 * for(int cnt=0;cnt<100;cnt++) {
		 * stmt.executeQuery("insert into dept02 values("+cnt+",'디자인부','경기도')"); }
		 */
		//sql디벨러퍼에서는 커밋이 필수, 외부java클래스 인서트할때는 자동커밋
		//테이블에 입력되어있는 레코드를 select 쿼리 stmt문장으로 가져옴 (아래 )
		ResultSet rs =stmt.executeQuery("select * from dept order by deptno ");
		//위에서 저장된 rs객체를 반복문을 출력(아래)
		while(rs.next()) {
			//rs객체의 레코드가 없을때까지 반복
			logger.debug(rs.getString("deptno")+" "+rs.getString("dname")+
					" "+rs.getString("loc"));
		}
		stmt = null;//메모리 반환
		rs = null;//메모리 반환
		connection = null;//메모리 초기화
	}
	@Test
	public void dbConnectionTest() {
		//데이터베이스 커넥션 테스트:설정은  root-context의 빈을 이용
		try {
			Connection connection = dataSource.getConnection();
			logger.debug("데이터베이스 접속이 성공 하였습니다. DB종류는 "+ connection.getMetaData().getDatabaseProductName());;
		} catch (SQLException e) {
			logger.debug("데이터베이스 접속이 실패 하였습니다.");
			//e.printStackTrace();
		}
		
		
	}
	@Test
	public void junitTest() {
		// 로거 장점 :조건에 따라서 출력을 조정할수있음
		logger.debug("Junit테스트시작입니다");
	}
}