package org.svnadmin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.svnadmin.Constants;
import org.svnadmin.entity.Pj;

/**
 * 项目
 * 
 * @author Harvey
 * 
 */
@Repository(PjDao.BEAN_NAME)
public class PjDao extends Dao {
	private final Logger LOG = Logger.getLogger(this.getClass());
	public static final String BEAN_NAME = "pjDao";

	/**
	 * @param pj
	 *            项目
	 * @return 项目
	 */
	public Pj get(String pj) {
		String sql = "select pj,path,url,des,type from pj where pj = ?";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			pstmt = conn.prepareStatement(sql);
			int index = 1;
			pstmt.setString(index++, pj);

			rs = pstmt.executeQuery();
			if (rs.next()) {
				return readPj(rs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			this.close(rs, pstmt, conn);
		}
		return null;
	}

	/**
	 * @return 项目列表
	 */
	public List<Pj> getList() {
		String sql = "select pj,path,url,des,type from pj order by pj";
		List<Pj> list = new ArrayList<Pj>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			pstmt = conn.prepareStatement(sql);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				list.add(readPj(rs));
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			this.close(rs, pstmt, conn);
		}
	}

	/**
	 * @param usr
	 *            用户
	 * @return 用户有权限的项目列表(用户是否是这个项目的管理员)
	 */
	public List<Pj> getList(String usr) {
		String sql = "select p.pj,p.path,p.url,p.des,p.type,pm.pj manager from ( "
				+ "select distinct a.pj,a.path,a.url,a.des,a.type from pj a where  "
				+ "exists (select b.usr from pj_gr_usr b where a.pj=b.pj and b.usr=?)  "
				+ "or exists(select c.usr from pj_usr_auth c where a.pj=c.pj and c.usr=?) "
				+ ") p "
				+ "left join ( "
				+ "select distinct a.pj from pj a where  "
				// TODO like ? 应该是 = ? ,用like主要是兼容3.0版本 see: Issue 4
				+ "exists (select b.usr from pj_gr_usr b where a.pj=b.pj and b.usr=? and b.gr like ?)"
				+ ") pm on p.pj=pm.pj";
		List<Pj> list = new ArrayList<Pj>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			pstmt = conn.prepareStatement(sql);
			int index = 1;
			pstmt.setString(index++, usr);
			pstmt.setString(index++, usr);
			pstmt.setString(index++, usr);
			pstmt.setString(index++, "%"+Constants.GROUP_MANAGER);//TODO 主要是兼容3.0版本

			rs = pstmt.executeQuery();
			while (rs.next()) {
				Pj pj = readPj(rs);
				String manager = rs.getString("manager");//是否是管理员组的用户
				pj.setManager(StringUtils.isNotBlank(manager));
				list.add(pj);
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			this.close(rs, pstmt, conn);
		}
	}

	public Pj readPj(ResultSet rs) throws SQLException {
		Pj result = new Pj();
		result.setPj(rs.getString("pj"));
		result.setPath(rs.getString("path"));
		result.setUrl(rs.getString("url"));
		result.setDes(rs.getString("des"));
		result.setType(rs.getString("type"));
		return result;
	}

	/**
	 * 删除
	 * 
	 * @param pj
	 */
	public void delete(String pj) {
		String sql = "delete from pj where pj = ?";
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = this.getConnection();
			pstmt = conn.prepareStatement(sql);
			int index = 1;
			pstmt.setString(index++, pj);

			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			this.close(null, pstmt, conn);
		}
	}

	public int insert(Pj pj) {
		String sql = "insert into pj (pj,path,url,des,type) values (?,?,?,?,?)";

		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = this.getConnection();
			pstmt = conn.prepareStatement(sql);
			int index = 1;
			pstmt.setString(index++, pj.getPj());
			pstmt.setString(index++, pj.getPath());
			pstmt.setString(index++, pj.getUrl());
			pstmt.setString(index++, pj.getDes());
			pstmt.setString(index++, pj.getType());

			return pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			this.close(null, pstmt, conn);
		}
	}

	public int update(Pj pj) {
		String sql = "update pj set path=?,url=?,des=?,type=? where pj = ?";

		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = this.getConnection();
			pstmt = conn.prepareStatement(sql);
			int index = 1;
			pstmt.setString(index++, pj.getPath());
			pstmt.setString(index++, pj.getUrl());
			pstmt.setString(index++, pj.getDes());
			pstmt.setString(index++, pj.getType());
			pstmt.setString(index++, pj.getPj());

			return pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			this.close(null, pstmt, conn);
		}
	}

	public int getCount(String path, String url) {
		String sql = "select count(1) from pj where path=? or url=?";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			pstmt = conn.prepareStatement(sql);
			int index = 1;
			pstmt.setString(index++, path);
			pstmt.setString(index++, url);

			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			this.close(rs, pstmt, conn);
		}
		return 0;
	}
}