# This is a server based program

Server was designed to run on the CS servers of SUNY Oswego, Gee specifically. Can be modified, change line 29 of the Client>Main.java to your desired server address, recompile and run after.
>29. address = (InetAddress.getByName("gee.cs.oswego.edu"));


# Files

Both the Clients and Server need the Diffie-Hellman key exchange class, however the Server keeps track of clients moves and board pieces, whereas the clients only keep track of the moves as they are played. The client has a interactive JFrame in a Grid Layout with all of the board pieces clickable.

