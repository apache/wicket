/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.cdapp.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.dialect.Dialect;
import net.sf.hibernate.impl.SessionFactoryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.examples.cdapp.model.CD;
import wicket.examples.cdapp.model.Category;
import wicket.examples.cdapp.model.Track;


/**
 * Utility that sets up the database.
 */
public final class DatabaseUtil
{
	/** Log. */
	private static Log log = LogFactory.getLog(DatabaseUtil.class);

	/** hibernate configuration. */
	private final Configuration configuration;

	/**
	 * Construct.
	 * @param configuration hibernate configuration
	 */
	public DatabaseUtil(Configuration configuration)
	{
		this.configuration = configuration;
	}

	/**
	 * (Re-)creates the database.
	 */
	public void createDatabase()
	{
		Session session = null;
		try
		{
			SessionFactory sessionFactory = configuration.buildSessionFactory();
			Dialect dialect = ((SessionFactoryImpl)sessionFactory).getDialect();
			String[] drops = configuration.generateDropSchemaScript(dialect);
			String[] creates = configuration.generateSchemaCreationScript(dialect);
			session = sessionFactory.openSession();
			Connection conn = session.connection();
			Statement stmt = conn.createStatement();
			execStmt(conn, stmt, splitAltTables(drops, true));
			execStmt(conn, stmt, splitAltTables(drops, false));
			execStmt(conn, stmt, creates);
			insertTestData(session);
		}
		catch (HibernateException e)
		{
			throw new RuntimeException(e);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				session.close();
			}
			catch (HibernateException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Insert a couple of test cd's.
	 * @param session hibernate session
	 * @throws HibernateException
	 */
	private void insertTestData(Session session) throws HibernateException
	{
		int count = 0;
		CD cd;
		List tracks;
		Set categories;
		String artist;
		Category catPop = newCategory(session, "pop");
		Category catRock = newCategory(session, "rock");
		Category catClassical = newCategory(session, "classical");
		Category catJazz = newCategory(session, "jazz");
		Category catElevator = newCategory(session, "elevator");
		Category catRnB = newCategory(session, "r&b");
		Category catMetal = newCategory(session, "metal");
		Category catDance = newCategory(session, "dance");
		Category catElectro = newCategory(session, "electro");
		Category catSoul = newCategory(session, "soul");
		Category catWorld = newCategory(session, "world");
		Category catCompilation = newCategory(session, "compilation");
		Category catAvantGarde = newCategory(session, "avant garde");
		Category catSingerSongwriter = newCategory(session, "singer songwriter");

		artist = "Lhasa";
		tracks = newTracks(session, new Track[] { new Track(1, "De Cara A La Pared", 4.16, artist),
				new Track(2, "La Celestina", 4.47, artist), new Track(3, "El Desierto", 3.54, artist),
				new Track(4, "Por Eso Me Quedo", 3.51, artist),
				new Track(5, "El Payande", 3.31, artist), new Track(6, "Los Peces", 3.53, artist),
				new Track(7, "Floricanto", 4.10, artist), new Track(8, "Desdenosa", 4.35, artist),
				new Track(9, "El Pajaro", 3.58, artist), new Track(10, "Mi Vanidad", 4.13, artist),
				new Track(11, "El Arbol Del Olvido", 3.13, artist) });
		categories = asSet(new Category[] { catPop, catWorld });
		cd = new CD("La LLorona", artist, "Audiogramm", "", 1997, tracks, categories, 5);
		session.save(cd);
		count++;

		artist = "Ben Folds Five";
		tracks = newTracks(session, new Track[] {
				new Track(1, "One Angry Dwarf and 200 Solemn Faces", 3.51, artist),
				new Track(2, "Fair", 5.51, artist), new Track(3, "Brick", 4.41, artist),
				new Track(4, "Song for the Dumped", 3.51, artist), new Track(5, "?", 1.33, artist),
				new Track(6, "Smoke", 4.52, artist), new Track(7, "Cigarette", 1.37, artist),
				new Track(8, "Steve's Last Night in Town", 3.27, artist),
				new Track(9, "Battle of Who Could Care Less", 3.16, artist),
				new Track(10, "Missing the War", 4.19, artist),
				new Track(11, "Evaporated", 5.41, artist) });
		categories = asSet(new Category[] { catPop });
		cd = new CD("One Angry Dwarf and 200 Solemn Faces", artist, "Caroline Records", "", 1995,
				tracks, categories, 5);
		session.save(cd);
		count++;

		artist = "Steely Dan";
		tracks = newTracks(session, new Track[] { new Track(1, "Do It Again", 5.56, artist),
				new Track(2, "Dirty Work", 3.45, artist), new Track(3, "Kings", 3.45, artist),
				new Track(4, "Midnite Cruiser", 4.09, artist),
				new Track(5, "Only A Fool Would Say That", 2.54, artist),
				new Track(6, "Reeling In The Years", 4.35, artist),
				new Track(7, "Fire In The Hole", 3.26, artist), new Track(8, "Brooklyn", 4.2, artist),
				new Track(9, "Change Of The Guard", 3.28, artist),
				new Track(10, "Turn That Heartbeat Over Again", 4.58, artist),
				new Track(11, "Wasted", 2.3, artist) });
		categories = asSet(new Category[] { catPop });
		cd = new CD("Cant Buy A Thrill", artist, "MCA Records", "", 1972, tracks, categories, 4);
		session.save(cd);
		count++;

		artist = "Steely Dan";
		tracks = newTracks(session,
				new Track[] { new Track(1, "Babylon Sisters", 5.56, artist),
						new Track(2, "Hey Nineteen", 3.45, artist),
						new Track(3, "Glamour Profession", 3.45, artist),
						new Track(4, "Third World Man", 4.09, artist),
						new Track(5, "Gaucho", 2.54, artist),
						new Track(6, "Time Out Of Mind", 4.35, artist),
						new Track(7, "My Rival", 3.26, artist) });
		categories = asSet(new Category[] { catPop });
		cd = new CD("Caucho", artist, "MCA Records", "", 1980, tracks, categories, 4);
		session.save(cd);
		count++;

		artist = "Paco De Lucia";
		tracks = newTracks(session, new Track[] { new Track(1, "Danza De Los Vecinos", 3.09, artist),
				new Track(2, "Danza Ritual Del Fuego", 4.24, artist),
				new Track(3, "Introduccion Y Pantominma", 2.59, artist),
				new Track(4, "El Pano Moruno", 1.27, artist),
				new Track(5, "Danza Del Molinero", 3.04, artist), new Track(6, "Danza", 3.24, artist),
				new Track(7, "Escena", 1.25, artist),
				new Track(8, "Cancion Del Fuego Fatuo", 4.05, artist),
				new Track(9, "Danza Del Terror", 1.48, artist),
				new Track(10, "Danza De La Molinera", 4.01, artist) });
		categories = asSet(new Category[] { catWorld, catClassical, catJazz });
		cd = new CD("Paco De Lucia Plays De Falla", artist, "PolyGram", "", 1978, tracks, categories,
				5);
		session.save(cd);
		count++;

		artist = "John Scofield";
		tracks = newTracks(session, new Track[] { new Track(1, "Lazy", 6.50, artist),
				new Track(2, "Peculiar", 6.33, artist), new Track(3, "Let The Cat Out", 5.35, artist),
				new Track(4, "Kool", 4.49, artist), new Track(5, "Old Soul", 5.21, artist),
				new Track(6, "Groove Elation", 6.50, artist), new Track(7, "Carlos", 7.28, artist),
				new Track(8, "Soft Shoe", 6.06, artist), new Track(9, "Let It Shine", 6.04, artist),
				new Track(10, "Bigtop", 6.33, artist) });
		categories = asSet(new Category[] { catJazz });
		cd = new CD("Groove Elation", artist, "Blue Note", "", 1995, tracks, categories, 4);
		session.save(cd);
		count++;

		artist = "Steps Ahead";
		tracks = newTracks(session, new Track[] { new Track(1, "Well In That Case", 6.50, artist),
				new Track(2, "Lust For Life", 6.33, artist),
				new Track(3, "Senegal Calling", 5.35, artist),
				new Track(4, "Red Neon, Go Or Give", 4.49, artist),
				new Track(5, "Charanga", 5.21, artist), new Track(6, "Get It", 6.50, artist),
				new Track(7, "N.Y.C.", 7.28, artist), new Track(8, "Stick Jam", 6.06, artist),
				new Track(9, "Absolutely Maybe", 6.04, artist),
				new Track(10, "Festival", 6.33, artist), new Track(11, "Paradiso", 3.33, artist) });
		categories = asSet(new Category[] { catJazz });
		cd = new CD("N.Y.C.", artist, "Intuition", "", 1989, tracks, categories, 5);
		session.save(cd);
		count++;

		artist = "Krang/ Andre Manuel";
		tracks = newTracks(session, new Track[] { new Track(1, "Kreupelen", 2.33, artist),
				new Track(2, "Plet", 0.27, artist), new Track(3, "Druif", 1.59, artist),
				new Track(4, "Moederneuker", 2.35, artist), new Track(5, "Nichts", 0.25, artist),
				new Track(6, "Platzpatrone", 1.24, artist), new Track(7, "Ruumte", 0.56, artist),
				new Track(8, "Billie Boem", 2.34, artist), new Track(9, "Vuurwater", 2.03, artist),
				new Track(10, "Banzaai", 1.34, artist), new Track(11, "Roes", 3.26, artist),
				new Track(12, "Boeddha", 3.54, artist), new Track(13, "Orentintel", 2.46, artist),
				new Track(14, "Naaldhakken", 4.20, artist), new Track(15, "Geil", 3.34, artist),
				new Track(16, "Pad", 2.34, artist), new Track(17, "Goot", 4.08, artist) });
		categories = asSet(new Category[] { catAvantGarde, catWorld, catSingerSongwriter });
		cd = new CD("Roes", artist, "Virgin", "", 1999, tracks, categories, 5);
		session.save(cd);
		count++;

		artist = "Bjork";
		tracks = newTracks(session, new Track[] { new Track(1, "Army Of Me", 1, artist),
				new Track(2, "Hyper-balad", 2, artist), new Track(3, "The Mordern Things", 3, artist),
				new Track(4, "It's Oh So Quiet", 4, artist), new Track(5, "Enjoy", 5, artist),
				new Track(6, "You've Been Flirting Again", 6, artist),
				new Track(7, "Isobel", 7, artist), new Track(8, "Possibly Maybe", 8, artist),
				new Track(9, "I Miss You", 9, artist), new Track(10, "Cover Me", 10, artist),
				new Track(11, "Headphones", 11, artist) });
		categories = asSet(new Category[] { catPop, catElectro });
		cd = new CD("Post", artist, "Mother Records", "", 1995, tracks, categories, 4);
		session.save(cd);
		count++;

		artist = "Van Halen";
		tracks = newTracks(session, new Track[] { new Track(1, "1984", 1.07, artist),
				new Track(2, "Jump", 4.04, artist), new Track(3, "Panama", 3.31, artist),
				new Track(4, "Top Jimmy", 2.59, artist), new Track(5, "Drop Dead Legs", 4.13, artist),
				new Track(6, "Hot For Teacher", 4.42, artist), new Track(7, "I'll Wait", 4.41, artist),
				new Track(8, "Girl Gone Bad", 4.33, artist),
				new Track(9, "House Of Pain", 3.18, artist) });
		categories = asSet(new Category[] { catRock });
		cd = new CD("1984", artist, "Warner Bros", "", 1983, tracks, categories, 5);
		session.save(cd);
		count++;

		artist = "Queens Of The Stone Age";
		tracks = newTracks(session, new Track[] {
				new Track(1, "You Think I Ain't Worth A Dollar But I Feel Like A Millionaire", 0,
						artist), new Track(2, "No One Knows", 0, artist),
				new Track(3, "First It Giveth", 0, artist),
				new Track(4, "A Song For The Dead", 0, artist),
				new Track(5, "The Sky Is Fallin'", 0, artist), new Track(6, "Six Shooter", 0, artist),
				new Track(7, "Hangin' AbstractTree", 0, artist), new Track(8, "Go With The Flow", 0, artist),
				new Track(9, "Gonna Leave You", 0, artist), new Track(10, "Do It Again", 0, artist),
				new Track(11, "God Is In The Radio", 0, artist),
				new Track(12, "Another Love Song", 0, artist),
				new Track(13, "A Song For The Deaf", 0, artist) });
		categories = asSet(new Category[] { catRock });
		cd = new CD("Songs For The Deaf", artist, "Interscope", "", 2002, tracks, categories, 3);
		session.save(cd);
		count++;

		artist = "Coparck";
		tracks = newTracks(session, new Track[] { new Track(1, "Awake", 0, artist),
				new Track(2, "Insert Space", 0, artist), new Track(3, "Into Routine", 0, artist),
				new Track(4, "Filling Holes", 0, artist), new Track(5, "Easy Install", 0, artist),
				new Track(6, "and Still Not Worried", 0, artist),
				new Track(7, "The Wrong Title For The Right Song", 0, artist),
				new Track(8, "Sustain", 0, artist), new Track(9, "Cynical Flowers", 0, artist),
				new Track(10, "The Society Swingers Of The Jet Set", 0, artist),
				new Track(11, "The Inevitable Return Of Happiness", 0, artist),
				new Track(12, "Better Sound Through Research", 0, artist),
				new Track(13, "Not Everyday's The Same", 0, artist),
				new Track(14, "First Case", 0, artist),
				new Track(15, "Song For A Next Album (...Asleep)", 0, artist) });
		categories = asSet(new Category[] { catPop });
		cd = new CD("Birds, Happiness and Still Not Worried", artist, "Labels Holland", "", 2002,
				tracks, categories, 5);
		session.save(cd);
		count++;

		artist = "Jeff Buckley";
		tracks = newTracks(session, new Track[] { new Track(1, "Mojo Pin", 0, artist),
				new Track(2, "Grace", 0, artist), new Track(3, "Last Goodbye", 0, artist),
				new Track(4, "Lilac Wine", 0, artist), new Track(5, "So Real", 0, artist),
				new Track(6, "Hallelujah", 0, artist),
				new Track(7, "Lover, You Should've Come Over", 0, artist),
				new Track(8, "Corpus Christi Carol", 0, artist),
				new Track(9, "Eternal Live", 0, artist), new Track(10, "Dream Brother", 0, artist) });
		categories = asSet(new Category[] { catPop, catSingerSongwriter });
		cd = new CD("Grace", artist, "Sony Music", "", 1994, tracks, categories, 5);
		session.save(cd);
		count++;

		artist = "Gomez";
		tracks = newTracks(session, new Track[] { new Track(1, "Hangover", 3.27, artist),
				new Track(2, "Revolutionary Kind", 4.31, artist),
				new Track(3, "Bring It On", 4.10, artist),
				new Track(4, "Blue Moon Rising", 4.48, artist),
				new Track(5, "Las Vegas Dealer", 3.54, artist),
				new Track(6, "We Haven't Turned Around", 6.29, artist),
				new Track(7, "Fill My Cup", 4.38, artist),
				new Track(8, "Rhythm And Blues Alibi", 5.03, artist),
				new Track(9, "Rosalita", 4.04, artist), new Track(10, "California", 7.23, artist),
				new Track(11, "Devil Will Ride", 6.56, artist) });
		categories = asSet(new Category[] { catPop });
		cd = new CD("Liquid Skin", artist, "Virgin", "", 1999, tracks, categories, 4);
		session.save(cd);
		count++;

		artist = "Bettie Serveert";
		tracks = newTracks(session, new Track[] { new Track(1, "Wide Eyed Fools", 0, artist),
				new Track(2, "Smack", 0, artist), new Track(3, "Have A Heart", 0, artist),
				new Track(4, "Captain Of Maybe", 0, artist), new Track(5, "De Diva", 0, artist),
				new Track(6, "Given", 0, artist), new Track(7, "Not Coming Down", 0, artist),
				new Track(8, "Cut 'n Dried", 0, artist), new Track(9, "Log 22", 0, artist),
				new Track(10, "White Dogs", 0, artist), new Track(11, "Certainlie", 0, artist),
				new Track(12, "The Ocean, My Floor", 0, artist),
				new Track(13, "The Love-In", 0, artist) });
		categories = asSet(new Category[] { catPop });
		cd = new CD("Log 22", artist, "Palomine", "", 2003, tracks, categories, 4);
		session.save(cd);
		count++;

		artist = "Nick Drake";
		tracks = newTracks(session, new Track[] { new Track(1, "Pink Moon", 2.00, artist),
				new Track(2, "Place To Be", 2.39, artist), new Track(3, "Road", 1.58, artist),
				new Track(4, "Which Will", 2.56, artist), new Track(5, "Horn", 1.19, artist),
				new Track(6, "Things Behind The Sun", 3.23, artist),
				new Track(7, "Know", 2.23, artist), new Track(8, "Parasite", 3.30, artist),
				new Track(9, "Ride", 2.57, artist), new Track(10, "Harvest Breed", 1.00, artist),
				new Track(11, "From The Morning", 2.25, artist) });
		categories = asSet(new Category[] { catPop, catSingerSongwriter });
		cd = new CD("Pink Moon", artist, "Island", "", 1972, tracks, categories, 4);
		session.save(cd);
		count++;

		artist = "Radiohead";
		tracks = newTracks(session, new Track[] {
				new Track(1, "Packt Like Sardines In A Crushd Tin Box", 0, artist),
				new Track(2, "Pyramid Song", 0, artist),
				new Track(3, "Pulk/ Pull Revolving Doors", 0, artist),
				new Track(4, "You And Whose Army?", 0, artist),
				new Track(5, "I Might Be Wrong", 0, artist), new Track(6, "Knives Out", 0, artist),
				new Track(7, "Morning Bell/ Amnesiac", 0, artist),
				new Track(8, "Dollars And Cents", 0, artist), new Track(9, "Hunting Bears", 0, artist),
				new Track(10, "Like Spinning Plates", 0, artist),
				new Track(11, "Life In A Glass House", 0, artist) });
		categories = asSet(new Category[] { catPop });
		cd = new CD("Amnesiac", artist, "EMI", "", 2001, tracks, categories, 5);
		session.save(cd);
		count++;

		artist = "Radiohead";
		tracks = newTracks(session, new Track[] {
				new Track(1, "Everything In Its Right Place", 0, artist),
				new Track(2, "Kid A", 0, artist), new Track(3, "The National Anthem", 0, artist),
				new Track(4, "How To Disappear Completely", 0, artist),
				new Track(5, "Treefingers", 0, artist), new Track(6, "Optimistic", 0, artist),
				new Track(7, "In Limbo", 0, artist), new Track(8, "Idioteque", 0, artist),
				new Track(9, "Morning Bell", 0, artist),
				new Track(10, "Motion Picture Soundtrack", 0, artist) });
		categories = asSet(new Category[] { catPop });
		cd = new CD("Kid A", artist, "EMI", "", 2001, tracks, categories, 5);
		session.save(cd);
		count++;

		log.info("saved " + count + " cds");
	}

	/**
	 * Create a new category.
	 * @param session Hibernate session
	 * @param name category name
	 * @return the new category
	 * @throws HibernateException
	 */
	private Category newCategory(Session session, String name) throws HibernateException
	{
		Category cat = new Category(name);
		session.save(cat);
		return cat;
	}

	/**
	 * Create new tracks.
	 * @param session Hibernate session
	 * @param tracks new tracks.
	 * @return list with the new tracks
	 * @throws HibernateException
	 */
	private List newTracks(Session session, Track[] tracks) throws HibernateException
	{
		List list = new ArrayList(tracks.length);
		for (int i = 0; i < tracks.length; i++)
		{
			session.save(tracks[i]);
			list.add(tracks[i]);
		}
		return list;
	}

	/**
	 * Wrap the categories in a set.
	 * @param cats the categories
	 * @return categories wrapped in a set
	 */
	private Set asSet(Category[] cats)
	{
		return new HashSet(Arrays.asList(cats));
	}

	/**
	 * Filter statements on start of statement.
	 * @param drops statements
	 * @param inclAlterFlag if true, everything that starts with alter, else the
	 *           inverse
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
	 * @param conn connection
	 * @param stmt statement object
	 * @param stmts statements
	 * @throws SQLException sql error
	 */
	private void execStmt(Connection conn, Statement stmt, String[] stmts) throws SQLException
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