/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */ 

package rat.report.analyser;

import java.io.StringReader;

import junit.framework.TestCase;
import rat.analysis.license.ApacheSoftwareLicense20;
import rat.report.analyser.HeaderCheckWorker;
import rat.report.claim.impl.xml.MockClaimReporter;

public class HeaderCheckWorkerTest extends TestCase {

	private static final String SOME_NAME = "Some Name";

	private static final String CONTENT_WITHOUT_LICENSE_FIRST_FIVE_LINES = 
	"'Great deeds, O friends! this wondrous man has wrought,\n" +
	"And mighty blessings to his country brought!\n" +
	"With ships he parted, and a numerous train,\n" +
	"Those, and their ships, he buried in the main.\n" +
	"Now he returns, and first essays his hand\n";
	
	private static final String CONTENT_WITHOUT_LICENSE = 
		CONTENT_WITHOUT_LICENSE_FIRST_FIVE_LINES +
"In the best blood of all his native land.\n" +
"Haste, then, and ere to neighbouring Pyle he flies,\n" +
"Or sacred Elis, to procure supplies;\n" +
"Arise (or ye for ever fall), arise!\n" +
"Shame to this age, and all that shall succeed!\n" +
"If unrevenged your sons and brothers bleed.\n" +
"Prove that we live, by vengeance on his head,\n" +
"Or sink at once forgotten with the dead.'\n" +
"Here ceased he, but indignant tears let fall\n" +
"Spoke when he ceased: dumb sorrow touch'd them all.\n" +
"When from the palace to the wondering throng\n" +
"Sage Medon came, and Phemius came along\n" +
"(Restless and early sleep's soft bands they broke);\n" +
"And Medon first the assembled chiefs bespoke;\n" +
"\n" +
"'Hear me, ye peers and elders of the land,\n" +
"Who deem this act the work of mortal hand;\n" +
"As o'er the heaps of death Ulysses strode,\n" +
"These eyes, these eyes beheld a present god,\n" +
"Who now before him, now beside him stood,\n" +
"Fought as he fought, and mark'd his way with blood:\n" +
"In vain old Mentor's form the god belied;\n" +
"'Twas Heaven that struck, and Heaven was on his side.'\n" +
"\n" +
"A sudden horror all the assembly shook,\n" +
"When slowly rising, Halitherses spoke\n" +
"(Reverend and wise, whose comprehensive view\n" +
"At once the present and the future knew):\n" +
"'Me too, ye fathers, hear! from you proceed\n" +
"The ills ye mourn; your own the guilty deed.\n" +
"Ye gave your sons, your lawless sons, the rein\n" +
"(Oft warn'd by Mentor and myself in vain);\n" +
"An absent hero's bed they sought to soil,\n" +
"An absent hero's wealth they made their spoil;\n" +
"Immoderate riot, and intemperate lust!\n" +
"The offence was great, the punishment was just.\n" +
"Weigh then my counsels in an equal scale,\n" +
"Nor rush to ruin. Justice will prevail.'\n" +
"\n" +
"His moderate words some better minds persuade:\n" +
"They part, and join him: but the number stay'd.\n" +
"They storm, they shout, with hasty frenzy fired,\n" +
"And second all Eupithes' rage inspired.\n" +
"They case their limbs in brass; to arms they run;\n" +
"The broad effulgence blazes in the sun.\n" +
"Before the city, and in ample plain,\n" +
"They meet: Eupithes heads the frantic train.\n" +
"Fierce for his son, he breathes his threats in air;\n" +
"Fate bears them not, and Death attends him there.\n" +
"\n" +
"This pass'd on earth, while in the realms above\n" +
"Minerva thus to cloud-compelling Jove!\n" +
"'May I presume to search thy secret soul?\n" +
"O Power Supreme, O Ruler of the whole!\n" +
"Say, hast thou doom'd to this divided state\n" +
"Or peaceful amity or stern debate?\n" +
"Declare thy purpose, for thy will is fate.'\n" +
"\n" +
"'Is not thy thought my own? (the god replies\n" +
"Who rolls the thunder o'er the vaulted skies;)\n" +
"Hath not long since thy knowing soul decreed\n" +
"The chief's return should make the guilty bleed.\n" +
"'Tis done, and at thy will the Fates succeed.\n" +
"Yet hear the issue: Since Ulysses' hand\n" +
"Has slain the suitors, Heaven shall bless the land.\n" +
"None now the kindred of the unjust shall own;\n" +
"Forgot the slaughter'd brother and the son:\n" +
"Each future day increase of wealth shall bring,\n" +
"\n" +
"And o'er the past Oblivion stretch her wing.\n" +
"Long shall Ulysses in his empire rest,\n" +
"His people blessing, by his people bless'd.\n" +
"Let all be peace.'--He said, and gave the nod\n" +
"That binds the Fates; the sanction of the god\n" +
"And prompt to execute the eternal will,\n" +
"Descended Pallas from the Olympian hill.\n" +
"\n" +
"Now sat Ulysses at the rural feast\n" +
"The rage of hunger and of thirst repress'd:\n" +
"To watch the foe a trusty spy he sent:\n" +
"A son of Dolius on the message went,\n" +
"Stood in the way, and at a glance beheld\n" +
"The foe approach, embattled on the field.\n" +
"With backward step he hastens to the bower,\n" +
"And tells the news. They arm with all their power.\n" +
"Four friends alone Ulysses' cause embrace,\n" +
"And six were all the sons of Dolius' race:\n" +
"Old Dolius too his rusted arms put on;\n" +
"And, still more old, in arms Laertes shone.\n" +
"Trembling with warmth, the hoary heroes stand,\n" +
"And brazen panoply invests the band.\n" +
"The opening gates at once their war display:\n" +
"Fierce they rush forth: Ulysses leads the way.\n" +
"That moment joins them with celestial aid,\n" +
"In Mentor's form, the Jove-descended maid:\n" +
"The suffering hero felt his patient breast\n" +
"Swell with new joy, and thus his son address'd:\n" +
"\n" +
"'Behold, Telemachus! (nor fear the sight,)\n" +
"The brave embattled, the grim front of fight!\n" +
"The valiant with the valiant must contend.\n" +
"Shame not the line whence glorious you descend.\n" +
"Wide o'er the world their martial fame was spread;\n" +
"Regard thyself, the living and the dead.'\n" +
"\n" +
"'Thy eyes, great father! on this battle cast,\n" +
"Shall learn from me Penelope was chaste.'\n" +
"\n" +
"So spoke Telemachus: the gallant boy\n" +
"Good old Laertes heard with panting joy.\n" +
"'And bless'd! thrice bless'd this happy day! (he cries,)\n" +
"The day that shows me, ere I close my eyes,\n" +
"A son and grandson of the Arcesian name\n" +
"Strive for fair virtue, and contest for fame!'\n" +
"\n" +
"Then thus Minerva in Laertes' ear:\n" +
"'Son of Arcesius, reverend warrior, hear!\n" +
"Jove and Jove's daughter first implore in prayer,\n" +
"Then, whirling high, discharge thy lance in air.'\n" +
"She said, infusing courage with the word.\n" +
"Jove and Jove's daughter then the chief implored,\n" +
"And, whirling high, dismiss'd the lance in air.\n" +
"Full at Eupithes drove the deathful spear:\n" +
"The brass-cheek'd helmet opens to the wound;\n" +
"He falls, earth thunders, and his arms resound.\n" +
"Before the father and the conquering son\n" +
"Heaps rush on heaps, they fight, they drop, they run\n" +
"Now by the sword, and now the javelin, fall\n" +
"The rebel race, and death had swallow'd all;\n" +
"But from on high the blue-eyed virgin cried;\n" +
"Her awful voice detain'd the headlong tide:\n" +
"'Forbear, ye nations, your mad hands forbear\n" +
"From mutual slaughter; Peace descends to spare.'\n" +
"Fear shook the nations: at the voice divine\n" +
"They drop their javelins, and their rage resign.\n" +
"All scatter'd round their glittering weapons lie;\n" +
"Some fall to earth, and some confusedly fly.\n" +
"With dreadful shouts Ulysses pour'd along,\n" +
"Swift as an eagle, as an eagle strong.\n" +
"But Jove's red arm the burning thunder aims:\n" +
"Before Minerva shot the livid flames;\n" +
"Blazing they fell, and at her feet expired;\n" +
"Then stopped the goddess, trembled and retired.\n" +
"\n" +
"'Descended from the gods! Ulysses, cease;\n" +
"Offend not Jove: obey, and give the peace.'\n" +
"\n" +
"So Pallas spoke: the mandate from above\n" +
"The king obey'd. The virgin-seed of Jove,\n" +
"In Mentor's form, confirm'd the full accord,\n" +
"And willing nations knew their lawful lord.";
	
	private static final String XML_CONTENT_WITH_LICENSE = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
	"<!--\n" +
	"   Copyright 2006 The Apache Software Foundation\n" +
	"   \n" +
	"   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
	"   you may not use this file except in compliance with the License.\n" +
	"   You may obtain a copy of the License at\n" +
	"   \n" +
	"       http://www.apache.org/licenses/LICENSE-2.0\n" +
	"    	   \n" +
	"   Unless required by applicable law or agreed to in writing, software\n" +
	"   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
	"   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
	"   See the License for the specific language governing permissions and\n" +
	"   limitations under the License.\n" +
	"-->\n" +
	"<project>\n" +
	"  <pomVersion>3</pomVersion>\n" +
	"  <name>Whatever</name>\n" +
	"  <groupId>apache-whatever</groupId>\n" +
	"  <artifactId>whatever-madeup</artifactId>\n" +
	"  <currentVersion>0.1-SNAPSHOT</currentVersion>\n" +
	"  <inceptionYear>2006</inceptionYear>\n" +
	"  <shortDescription>Apache Whatever Madeup Name</shortDescription>\n" +
	"  <description>Example Snippet From Imaginary POM</description>\n" +
	"  <logo>/images/logo.png</logo>\n" +
	"</project>";
	
	private static final String XML_CONTENT_WITHOUT_LICENSE = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
	"<!--\n" +
	"   Copyright 2006 The Apache Software Foundation\n" +
	"   \n" +
	"-->\n" +
	"<project>\n" +
	"  <pomVersion>3</pomVersion>\n" +
	"  <name>Whatever</name>\n" +
	"  <groupId>apache-whatever</groupId>\n" +
	"  <artifactId>whatever-madeup</artifactId>\n" +
	"  <currentVersion>0.1-SNAPSHOT</currentVersion>\n" +
	"  <inceptionYear>2006</inceptionYear>\n" +
	"  <shortDescription>Apache Whatever Madeup Name</shortDescription>\n" +
	"  <description>Example Snippet From Imaginary POM</description>\n" +
	"  <logo>/images/logo.png</logo>\n" +
	"</project>";
	
	/**
	 * Excert from 
	 * http://svn.apache.org/repos/asf/jakarta/commons/proper/betwixt/trunk/project.properties
	 */
	private static final String PLAIN_CONTENT_WITH_LICENSE = 
	"#   Copyright 2006 The Apache Software Foundation\n" +
	"#\n" +
	"#   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
	"#   you may not use this file except in compliance with the License.\n" +
	"#   You may obtain a copy of the License at\n" +
	"#\n" +
	"#       http://www.apache.org/licenses/LICENSE-2.0\n" +
	"#\n" +
	"#   Unless required by applicable law or agreed to in writing, software\n" +
	"#   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
	"#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
	"#   See the License for the specific language governing permissions and\n" +
	"#   limitations under the License.\n" +
	"\n" +
	"# -------------------------------------------------------------------\n" +
	"# P R O J E C T  P R O P E R T I E S\n" +
	"# -------------------------------------------------------------------\n" +
	"\n" +
	"maven.changelog.factory=org.apache.maven.svnlib.SvnChangeLogFactory\n" +
	"\n" +
	"maven.compile.debug = on\n" +
	"maven.compile.optimize = off\n" +
	"maven.compile.deprecation = on\n" +
	"maven.compile.target = 1.1\n" +
	"maven.compile.source=1.3\n";
	
	
	/**
	 * Excert from 
	 * http://svn.apache.org/repos/asf/jakarta/commons/proper/betwixt/trunk/project.properties
	 */
	
	private static final String PLAIN_CONTENT_WITHOUT_LICENSE = 
	"#   Copyright 2006 The Apache Software Foundation\n" +
	"\n" +
	"# -------------------------------------------------------------------\n" +
	"# P R O J E C T  P R O P E R T I E S\n" +
	"# -------------------------------------------------------------------\n" +
	"\n" +
	"maven.changelog.factory=org.apache.maven.svnlib.SvnChangeLogFactory\n" +
	"\n" +
	"maven.compile.debug = on\n" +
	"maven.compile.optimize = off\n" +
	"maven.compile.deprecation = on\n" +
	"maven.compile.target = 1.1\n" +
	"maven.compile.source=1.3\n";
	
	/**
	 * Excert from 
	 * http://svn.apache.org/repos/asf/jakarta/commons/proper/betwixt/trunk/src/java/org/apache/commons/betwixt/XMLUtils.java
	 */
	private static final String CODE_WITH_LICENSE =
"/*\n" +
" * Copyright 2001-2004 The Apache Software Foundation.\n" +
" * \n" +
" * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
" * you may not use this file except in compliance with the License.\n" +
" * You may obtain a copy of the License at\n" +
" * \n" +
" *      http://www.apache.org/licenses/LICENSE-2.0\n" +
" * \n" +
" * Unless required by applicable law or agreed to in writing, software\n" +
" * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
" * See the License for the specific language governing permissions and\n" +
" * limitations under the License.\n" +
" */ \n" +
"package org.apache.commons.betwixt;\n" +
" /**\n" +
"  * <p><code>XMLUtils</code> contains basic utility methods for XML.</p>\n" +
"  * \n" +
"  * <p>The code for {@link #isWellFormedXMLName} is based on code in\n" + 
"  * <code>org.apache.xerces.util.XMLChar</code> \n" +
"  * in <a href='http://xml.apache.org/xerces2-j/index.html'>Apache Xerces</a>.\n" +
"  * The authors of this class are credited below.</p>\n" +
"  *\n" +
"  * @author Glenn Marcy, IBM\n" +
"  * @author Andy Clark, IBM\n" +
"  * @author Eric Ye, IBM\n" +
"  * @author Arnaud  Le Hors, IBM\n" +
"  * @author Rahul Srivastava, Sun Microsystems Inc.\n" +  
"  *\n" +
"  * @author Robert Burrell Donkin\n" +
"  * @since 0.5\n" +
"  */\n" +
"public class XMLUtils {";
	
	private static final String CODE_WITHOUT_LICENSE =
		"/*\n" +
		" * Copyright 2001-2004 The Apache Software Foundation.\n" +
		" * \n" +
		" */ \n" +
		"package org.apache.commons.betwixt;\n" +
		" /**\n" +
		"  * <p><code>XMLUtils</code> contains basic utility methods for XML.</p>\n" +
		"  * \n" +
		"  * <p>The code for {@link #isWellFormedXMLName} is based on code in\n" + 
		"  * <code>org.apache.xerces.util.XMLChar</code> \n" +
		"  * in <a href='http://xml.apache.org/xerces2-j/index.html'>Apache Xerces</a>.\n" +
		"  * The authors of this class are credited below.</p>\n" +
		"  *\n" +
		"  * @author Glenn Marcy, IBM\n" +
		"  * @author Andy Clark, IBM\n" +
		"  * @author Eric Ye, IBM\n" +
		"  * @author Arnaud  Le Hors, IBM\n" +
		"  * @author Rahul Srivastava, Sun Microsystems Inc.\n" +  
		"  *\n" +
		"  * @author Robert Burrell Donkin\n" +
		"  * @since 0.5\n" +
		"  */\n" +
		"public class XMLUtils {";
	
    MockClaimReporter reporter;
    
	protected void setUp() throws Exception {
		super.setUp();
        reporter = new MockClaimReporter();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testIsFinished() throws Exception {
		HeaderCheckWorker worker = new HeaderCheckWorker(new StringReader(""), new ApacheSoftwareLicense20(), reporter, "subject");
		assertFalse(worker.isFinished());
		worker.read();
		assertTrue(worker.isFinished());
	}
}
