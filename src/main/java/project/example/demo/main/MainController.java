package project.example.demo.main;

import java.io.File;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import project.example.demo.DTO.RestaurantDTO;

@Controller
public class MainController {
	@Autowired
	MainDAO mdao;

	@GetMapping("/main")
	public String main_page() {
		return "main";
	}
		
	@GetMapping("/main/search/{query}")
	public String detail(@PathVariable("query") String query) {
		return "main";
	}
	
	@PostMapping("/check/duplicateLocation")
	@ResponseBody
	public String check_duplicateLocation(HttpServletRequest req) {
		String address = req.getParameter("address");

		JSONArray ja = new JSONArray();
		ArrayList<RestaurantDTO> rdto = mdao.check_duplicateLocation(address);

		for (RestaurantDTO r : rdto) {
			JSONObject jo = new JSONObject();
			jo.put("r_name", r.getR_name());
			jo.put("category", r.getCategory());
			jo.put("address", r.getAddress());

			ja.put(jo);
		}
		return ja.toString();
	}
	
	@PostMapping("/suggest/alm/user")
	@ResponseBody
	public String suggest_alm (@RequestPart(value = "restaurant") RestaurantDTO rdto) {
		String message = "proceed";
		
		double lat = rdto.getLat();
		double lng = rdto.getLng();
		String primecode = null;
		String r_name = rdto.getR_name();
		String category = rdto.getCategory();
		String address = rdto.getAddress().replace("\"", "");
		String owner = null;
		String localURL = null;
		
		int duplicate = mdao.check_duplicateRequest(r_name, address);
		if (duplicate == 0) 
			mdao.restaurant_approval_request(lat, lng, primecode, r_name, owner, category, address, localURL);
		else message = "duplicate";
		
		return message;
	}
	
	@PostMapping("/suggest/alm/ceo")
	@ResponseBody
	public String suggest_alm (@RequestPart(value = "restaurant") RestaurantDTO rdto,
		   					   @RequestPart(value = "bnd") MultipartFile[] bnd,
		   					   HttpServletRequest req) {
		String message = "proceed";

		double lat = rdto.getLat();
		double lng = rdto.getLng();
		String primecode = rdto.getPrimecode();
		String r_name = rdto.getR_name();
		String category = rdto.getCategory();
		String address = rdto.getAddress().replace("\"", "");
		
		HttpSession session = req.getSession();
		String realname = mdao.get_member_name(String.valueOf(session.getAttribute("id")));
		String owner = String.valueOf(session.getAttribute("id")) + "," + realname;
		String idString = String.valueOf(session.getAttribute("id")) + " ";

		LocalDate today = LocalDate.now();
		DateTimeFormatter fd = DateTimeFormatter.ofPattern(" yyyy-MM-DD ");
		String todayString = today.format(fd);
		LocalTime now = LocalTime.now();
		DateTimeFormatter fn = DateTimeFormatter.ofPattern("HH-mm-ss ");
		String timeString = now.format(fn);
		
		String location = "C:\\Users\\leon1\\eclipse-workspace\\Project\\src\\main\\resources\\static\\img\\admin\\restaurant";
		String shortLocation = "/img/admin/restaurant/";
		
		String[] extensions = {
				"bmp", "jpg", "jpeg", "gif", "png", "webp", "webm", "jfif", "pdf"
		};
		String originalExtension = bnd[0].getOriginalFilename().split("\\.")[1];
		int acceptable = 0;
		for (String s : extensions) {
			if (s.equals(originalExtension)) {
				acceptable = 1; break;
			}
		}
		if (acceptable == 0) {
			message = "extension";
			return message;
		}
		
		String filename = r_name + " " + idString + todayString + timeString + bnd[0].getOriginalFilename();
		File savefile = new File(location, filename);
		try {
			bnd[0].transferTo(savefile);
		} catch (Exception e) {
		}
		String localURL = shortLocation + filename;
		
		int duplicate = mdao.check_duplicateRequest(r_name, address);
		if (duplicate == 0) 
			mdao.restaurant_approval_request(lat, lng, primecode, r_name, owner, category, address, localURL);
		else {
			mdao.restaurant_update_request(lat, lng, primecode, r_name, owner, category, address, localURL);
		}

		return message;
	}
	
	@PostMapping("/main/filter/search")
	@ResponseBody
	public String filter_search(HttpServletRequest req) {
		String words = req.getParameter("query");
		String fc = req.getParameter("fc");
		String ce = req.getParameter("ce");
		String ob = req.getParameter("ob");
		HttpSession session = req.getSession();
		String id = String.valueOf(session.getAttribute("id"));
		ArrayList<String> tags = new ArrayList<>();
		if (req.getParameterValues("tags[]") != null) {
			for (String s : req.getParameterValues("tags[]")) tags.add(s);
		}
		double lat = Double.parseDouble(req.getParameter("lat"));
		double lng = Double.parseDouble(req.getParameter("lng"));
				
		String query = make_searchFilterQuery(words, fc, ce, ob, id, tags, lat, lng);
		ArrayList<RestaurantDTO> rdto = mdao.get_searchFilterLIst(query);
		JSONArray ja = new JSONArray();
		System.out.println(query);
		for (RestaurantDTO r : rdto) {
			JSONObject jo = new JSONObject();
			jo.put("lat", r.getLat());
			jo.put("lng", r.getLng());
			jo.put("r_name", r.getR_name());
			jo.put("category", r.getCategory());
			jo.put("address", r.getAddress());
			jo.put("r_phone", r.getR_phone());
			jo.put("r_photo", r.getR_photo());
			ja.put(jo);
		}
		return ja.toString();
	}
	
	public String make_searchFilterQuery(String words, String fc, String ce, String ob, 
										 String id, ArrayList<String> tags,
										 double lat, double lng) {
		StringBuilder query = new StringBuilder();
		
		query.append("""
				select *
				from (
				""");
		
		if (ce.equals("close")) {
			query.append(String.format("""
						select a.*, abs((a.lat - %1$s) + (a.lng - %2$s)) as close
					""", lat, lng));
		}
		else if (ce.equals("eval")) {
			String temp = "\tselect a.*, ";
			for (int i = 0; i < tags.size(); i++) {
				if (i == tags.size() - 1) temp += "c." + tags.get(i) + " as eval\n";
				else temp += "c." + tags.get(i) + " + ";
			}
			query.append(temp);
		}
		
		query.append("""
					from (
						select *
						from restaurant
				""");
		
		if (! words.equals("")) {
			query.append(String.format("""
							where (r_name like '%%%1$s%%'
							or address like '%%%1$s%%')
					""", words));
		}
		
		if (fc != null) {
			if (words.equals("")) query.append("		where ");
			else query.append("		and ");
			query.append(String.format("""
					category = '%1$s'
					\t\t) a""",fc));
		}
		else query.append("\t\t) a");
		
		if (tags.size() != 0) query.append(", statistic c");
		
		if (ob != null) {
			if (ob.equals("been")) query.append(String.format("""
					, review b
						where a.r_name = b.rv_r_name
						and a.address = b.rv_address
						and b.rv_id = '%1$s'
					""", id));
			if (ob.equals("never")) query.append(String.format("""
					, (
							  select *
							  from review
							  where rv_id = '%1$s') b
						where a.r_name = b.rv_r_name(+)
						and a.address = b.rv_address(+)
						and (b.rv_id != '%1$s' 
						or b.rv_id is null)
					""", id));
		}
		
		if (tags.size() != 0) {
			if (ob != null) query.append("	and ");
			else query.append("\n	where ");
			query.append("""
					a.r_name = c.s_r_name
						and a.address = c.s_address
					""");
			for (String s : tags) {
				query.append(String.format("""
							and c.%1$s > 0
						""", s));
			}
		}
		
		query.append(String.format("""
			\torder by %1$s
			""", ce));
		
		query.append(")\nwhere rownum <= 10");
		
		return query.toString();
	}
}