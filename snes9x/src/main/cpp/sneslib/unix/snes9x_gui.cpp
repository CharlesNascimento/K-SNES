/*******************************************************************************
  Snes9x - Portable Super Nintendo Entertainment System (TM) emulator.
 
  (c) Copyright 1996 - 2002 Gary Henderson (gary.henderson@ntlworld.com) and
                            Jerremy Koot (jkoot@snes9x.com)

  (c) Copyright 2001 - 2004 John Weidman (jweidman@slip.net)

  (c) Copyright 2002 - 2004 Brad Jorsch (anomie@users.sourceforge.net),
                            funkyass (funkyass@spam.shaw.ca),
                            Joel Yliluoma (http://iki.fi/bisqwit/)
                            Kris Bleakley (codeviolation@hotmail.com),
                            Matthew Kendora,
                            Nach (n-a-c-h@users.sourceforge.net),
                            Peter Bortas (peter@bortas.org) and
                            zones (kasumitokoduck@yahoo.com)

  C4 x86 assembler and some C emulation code
  (c) Copyright 2000 - 2003 zsKnight (zsknight@zsnes.com),
                            _Demo_ (_demo_@zsnes.com), and Nach

  C4 C++ code
  (c) Copyright 2003 Brad Jorsch

  DSP-1 emulator code
  (c) Copyright 1998 - 2004 Ivar (ivar@snes9x.com), _Demo_, Gary Henderson,
                            John Weidman, neviksti (neviksti@hotmail.com),
                            Kris Bleakley, Andreas Naive

  DSP-2 emulator code
  (c) Copyright 2003 Kris Bleakley, John Weidman, neviksti, Matthew Kendora, and
                     Lord Nightmare (lord_nightmare@users.sourceforge.net

  OBC1 emulator code
  (c) Copyright 2001 - 2004 zsKnight, pagefault (pagefault@zsnes.com) and
                            Kris Bleakley
  Ported from x86 assembler to C by sanmaiwashi

  SPC7110 and RTC C++ emulator code
  (c) Copyright 2002 Matthew Kendora with research by
                     zsKnight, John Weidman, and Dark Force

  S-DD1 C emulator code
  (c) Copyright 2003 Brad Jorsch with research by
                     Andreas Naive and John Weidman
 
  S-RTC C emulator code
  (c) Copyright 2001 John Weidman
  
  ST010 C++ emulator code
  (c) Copyright 2003 Feather, Kris Bleakley, John Weidman and Matthew Kendora

  Super FX x86 assembler emulator code 
  (c) Copyright 1998 - 2003 zsKnight, _Demo_, and pagefault 

  Super FX C emulator code 
  (c) Copyright 1997 - 1999 Ivar, Gary Henderson and John Weidman


  SH assembler code partly based on x86 assembler code
  (c) Copyright 2002 - 2004 Marcus Comstedt (marcus@mc.pp.se) 

 
  Specific ports contains the works of other authors. See headers in
  individual files.
 
  Snes9x homepage: http://www.snes9x.com
 
  Permission to use, copy, modify and distribute Snes9x in both binary and
  source form, for non-commercial purposes, is hereby granted without fee,
  providing that this license information and copyright notice appear with
  all copies and any derived work.
 
  This software is provided 'as-is', without any express or implied
  warranty. In no event shall the authors be held liable for any damages
  arising from the use of this software.
 
  Snes9x is freeware for PERSONAL USE only. Commercial users should
  seek permission of the copyright holders first. Commercial use includes
  charging money for Snes9x or software derived from Snes9x.
 
  The copyright holders request that bug fixes and improvements to the code
  should be forwarded to them so everyone can benefit from the modifications
  in future versions.
 
  Super NES and Super Nintendo Entertainment System are trademarks of
  Nintendo Co., Limited and its subsidiary companies.
*******************************************************************************/
#include "snes9x_gui.h"

#include "snes9x.h"
#include "memmap.h"
#include "display.h"
#include "soundux.h"
#include "cpuexec.h"

#include <qpixmap.h>
#include <qtoolbar.h>
#include <qtoolbutton.h>
#include <qpopupmenu.h>
#include <qmenubar.h>
#include <qkeycode.h>
#include <qfile.h>
#include <qfiledialog.h>
#include <qstatusbar.h>
#include <qmessagebox.h>
#include <qapplication.h>
#include <qaccel.h>
#include <qtextstream.h>
#include <qpainter.h>
#include <qpaintdevicemetrics.h>
#include <qwhatsthis.h>

#include "filesave.xpm"
#include "fileopen.xpm"
#include "fileprint.xpm"

const char * fileOpenText = "Click this button to open a new file.\n\n"
"You can also select the Open command from the File menu.";
const char * fileSaveText = "Click this button to save the file you are "
"editing.  You will be prompted for a file name.\n\n"
"You can also select the Save command from the File menu.\n\n"
"Note that implementing this function is left as an exercise for the reader.";
const char * filePrintText = "Click this button to print the file you "
"are editing.\n\n"
"You can also select the Print command from the File menu.";

Snes9xGUI::Snes9xGUI()
    : QMainWindow( 0, "example snes9x_gui main window" )
{
    QPixmap openIcon, saveIcon, printIcon;

    fileTools = new QToolBar( this, "file operations" );

    openIcon = QPixmap( fileopen );
    QToolButton * fileOpen = new QToolButton( openIcon, "Open File", 0,
					      this, SLOT(load()),
					      fileTools, "open file" );

    saveIcon = QPixmap( filesave );
    QToolButton * fileSave = new QToolButton( saveIcon, "Save File", 0,
					      this, SLOT(save()),
					      fileTools, "save file" );

    printIcon = QPixmap( fileprint );
    QToolButton * reset = new QToolButton( printIcon, "Reset", 0,
					       this, SLOT(reset ()),
					       fileTools, "reset" );

    (void)QWhatsThis::whatsThisButton( fileTools );
    QWhatsThis::add( fileOpen, fileOpenText, FALSE );
    QWhatsThis::add( fileSave, fileSaveText, FALSE );
    QWhatsThis::add( reset, filePrintText, FALSE );
    
    QPopupMenu * file = new QPopupMenu();
    menuBar()->insertItem( "&File", file );

    file->insertItem( "New", this, SLOT(newDoc()), CTRL+Key_N );
    file->insertItem( openIcon, "Open", this, SLOT(load()), CTRL+Key_O );
    file->insertItem( saveIcon, "Save", this, SLOT(save()), CTRL+Key_S );
    file->insertSeparator();
    file->insertItem( printIcon, "Reset", this, SLOT(reset()), CTRL+Key_R );
    file->insertSeparator();
    file->insertItem( "Close", this, SLOT(closeDoc()), CTRL+Key_W );
    file->insertItem( "Quit", qApp, SLOT(quit()), CTRL+Key_Q );

    controls = new QPopupMenu();
    menuBar()->insertItem( "&Controls", controls );

    mb = controls->insertItem( "Menu bar", this, SLOT(toggleMenuBar()), CTRL+Key_M);
    // Now an accelerator for when the menubar is invisible!
    QAccel* a = new QAccel(this);
    a->connectItem( a->insertItem( CTRL+Key_M ), this, SLOT(toggleMenuBar()) );

    tb = controls->insertItem( "Tool bar", this, SLOT(toggleToolBar()), CTRL+Key_T);
    sb = controls->insertItem( "Status bar", this, SLOT(toggleStatusBar()), CTRL+Key_B);
    controls->setCheckable( TRUE );
    controls->setItemChecked( mb, TRUE );
    controls->setItemChecked( tb, TRUE );
    controls->setItemChecked( sb, TRUE );

    fileOpen->setFocus();
    //setCentralWidget( e );
    statusBar()->message( "Ready", 2000 );
}


Snes9xGUI::~Snes9xGUI()
{
    // no explicit destruction is necessary in this example
}

void Snes9xGUI::newDoc()
{
    Snes9xGUI *ed = new Snes9xGUI;
    ed->resize( 400, 400 );
    ed->show();
}

void Snes9xGUI::load()
{
    S9xSetSoundMute (TRUE);
    char *filter = "*.[SsZzFf0-9Aa][MmIi4754][CcPpGg8]";
    QString fn = QFileDialog::getOpenFileName (S9xGetROMDirectory (),filter,this);
    if ( !fn.isEmpty() )
	load( fn );
    else
    {
	statusBar()->message( "Loading aborted", 2000 );
    }
    S9xSetSoundMute (FALSE);
}

void Snes9xGUI::load( const char *fileName )
{
    Memory.SaveSRAM (S9xGetSRAMFilename ());
    if (Memory.LoadROM (fileName))
    {
	setCaption( fileName );
	QString s;
	s.sprintf( "Loaded document %s", fileName );
	statusBar()->message( s, 2000 );
    }
}

void Snes9xGUI::save()
{
    statusBar()->message( "File->Save is not implemented" );
    QMessageBox::message( "Note", "Left as an exercise for the user." );
}

void Snes9xGUI::reset ()
{
    if (QMessageBox::information (statusBar(), "Snes9x: Reset Query",
				  "\
Are you sure you want to reset the game?\n\
(You will still be able to returned to the game's\n\
 last battery-RAM save position or your last\n\
 freeze position.", "Yes", "No") == 0)
    {
	S9xReset ();
    }
}

void Snes9xGUI::closeDoc()
{
    S9xExit ();
}

void Snes9xGUI::toggleMenuBar()
{
    if ( menuBar()->isVisible() ) {
	menuBar()->hide();
	controls->setItemChecked( mb, FALSE );
    } else {
	menuBar()->show();
	controls->setItemChecked( mb, TRUE );
    }
}

void Snes9xGUI::toggleToolBar()
{
    if ( fileTools->isVisible() ) {
	fileTools->hide();
	controls->setItemChecked( tb, FALSE );
    } else {
	fileTools->show();
	controls->setItemChecked( tb, TRUE );
    }
}

void Snes9xGUI::toggleStatusBar()
{
    if ( statusBar()->isVisible() ) {
	statusBar()->hide();
	controls->setItemChecked( sb, FALSE );
    } else {
	statusBar()->show();
	controls->setItemChecked( sb, TRUE );
    }
}

