/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.guestbook2;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.dialect.Dialect;
import net.sf.hibernate.impl.SessionFactoryImpl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.examples.util.hibernate.ConfigException;
import wicket.examples.util.hibernate.HibernateHelper;

import java.io.IOException;

import java.net.URL;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility that sets up the database.
 */
public class DBUtil
{
	/** Log. */
	private static Log log = LogFactory.getLog(DBUtil.class);

	/** construct. */
	public DBUtil()
	{
		// no nada
	}

	/**
	 * Initialize and create the database.
	 * 
	 * @param configFile
	 * @throws ConfigException
	 *             configuration error
	 * @throws HibernateException
	 *             hibernate error
	 * @throws SQLException
	 * @throws IOException
	 */
	public void initDB(String configFile) throws HibernateException, ConfigException, IOException,
			SQLException
	{
		URL configUrl = DBUtil.class.getResource(configFile);

		HibernateHelper.setConfigURL(configUrl);
		HibernateHelper.init();

		Session session = HibernateHelper.getSession();
		Connection conn = session.connection();
		Statement stmt = conn.createStatement();
		boolean exists;

		try
		{
			stmt.executeQuery("select 1 from Comment");
			exists = true;
		}
		catch (SQLException e)
		{
			exists = false;
		}

		try
		{
			if ((!exists) || Boolean.getBoolean("createdb"))
			{
				SessionFactoryImpl sf = (SessionFactoryImpl)HibernateHelper.getSessionFactory();
				Dialect dialect = sf.getDialect();
				Configuration config = HibernateHelper.getConfiguration();
				String[] drops = config.generateDropSchemaScript(dialect);
				String[] creates = config.generateSchemaCreationScript(dialect);

				execStmt(conn, stmt, splitAltTables(drops, true));
				execStmt(conn, stmt, splitAltTables(drops, false));
				execStmt(conn, stmt, creates);

				insertTestData(session);
			}
		}
		finally
		{
			HibernateHelper.closeSession();
		}
	}

	/**
	 * Insert a couple of test cd's.
	 * 
	 * @param session
	 *            hibernate session
	 * @throws HibernateException
	 */
	private void insertTestData(Session session) throws HibernateException
	{
		Transaction transaction = session.beginTransaction();

		Comment comment = new Comment();

		comment.setText("Hi this is my first comment");
		comment.setDate(new Date(System.currentTimeMillis() - 10000));

		session.save(comment);

		comment = new Comment();
		comment.setText("Hi this is my second comment");
		comment.setDate(new Date(System.currentTimeMillis() - 5000));

		session.save(comment);

		transaction.commit();
	}

	/**
	 * Filter statements on start of statement.
	 * 
	 * @param drops
	 *            statements
	 * @param inclAlterFlag
	 *            if true, everything that starts with alter, else the inverse
	 * @return part of the input
	 */
	private String[] splitAltTables(String[] drops, boolean inclAlterFlag)
	{
		List temp = new ArrayList();

		for (int i = 0; i < drops.length; i++)
		{
			if (inclAlterFlag)
			{
				if (drops[i].toLowerCase().trim().startsWith("alter"))
				{
					temp.add(drops[i]);
				}
			}
			else
			{
				if (!drops[i].toLowerCase().trim().startsWith("alter"))
				{
					temp.add(drops[i]);
				}
			}
		}

		return (String[])temp.toArray(new String[temp.size()]);
	}

	/**
	 * Execute statements.
	 * 
	 * @param conn
	 *            connection
	 * @param stmt
	 *            statement object
	 * @param stmts
	 *            statements
	 * @throws SQLException
	 *             sql error
	 */
	public void execStmt(Connection conn, Statement stmt, String[] stmts) throws SQLException
	{
		for (int i = 0; i < stmts.length; i++)
		{
			log.info("exec: " + stmts[i]);

			try
			{
				stmt.executeUpdate(stmts[i]);
				conn.commit();
			}
			catch (SQLException e)
			{
				log.error(e.getMessage());
			}
		}
	}
}