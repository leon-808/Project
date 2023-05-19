<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>비밀번호 찾기</title>
<link href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css" rel="stylesheet">
<link href="/css/member/PwFind.css" rel="stylesheet">
</head>
<body class="text-center">
	<section>
		<div>
		<h1>비밀번호 찾기</h1>
		</div>
		<article>
			<input type="text" id="inputFindId" placeholder="아이디"><br>
			<input type="text" id="inputFindName" placeholder="이름"><br>
			<input type="number" id="inputFindPhone" placeholder="전화번호"><br>
			<pre id="SearchPwMessage"></pre>
			<button id="buttonFindPw">비밀번호 찾기</button>
		</article>
	</section>
</body>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="/js/member/PwFind.js"></script>
</html>