package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> moves = new ArrayList<>();
        if (isInCheckmate(piece.getTeamColor())) {
            return moves;
        }
        Collection<ChessMove> kingMoves = new ArrayList<>();
        TeamColor teamColor = this.teamTurn;
        for (int i = 1; i < board.getPieces().length + 1; i++) {
            for (int j = 1; j < board.getPieces().length + 1; j++) {
                if (board.getPieces()[8 - i][j - 1] != null) {
                    ChessPosition position = new ChessPosition(i, j);
                    if (board.getPiece(position).getPieceType() == ChessPiece.PieceType.KING && board.getPiece(position).getTeamColor() == teamColor) {
                        kingMoves = board.getPiece(position).pieceMoves(board, position);
                    }
                }
            }
        }
//        for (ChessMove move : kingMoves) {
//            if (willBeInCheck(piece.getTeamColor(), move)) {
//                ;
//            }
//        }
        moves = piece.pieceMoves(board, startPosition);


        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        if (board.getPieces()[8 - start.getRow()][start.getColumn() - 1] != null &&
                validMoves(start).contains(move) &&
                !isInCheck(board.getPiece(start).getTeamColor()) &&
                board.getPiece(start).getTeamColor() == teamTurn) {
            if (move.getPromotionPiece() == null) {
                board.addPiece(move.getEndPosition(), board.getPiece(start));
                board.addPiece(start, null);
            } else {
                board.addPiece(move.getEndPosition(), new ChessPiece(this.teamTurn, move.getPromotionPiece()));
                board.addPiece(start, null);
            }
            if (teamTurn == TeamColor.WHITE) {
                setTeamTurn(TeamColor.BLACK);
            } else {
                setTeamTurn(TeamColor.WHITE);
            }
        } else {
            throw new InvalidMoveException();
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        boolean inCheck = false;
        for (int i = 1; i < board.getPieces().length + 1; i++) {
            for (int j = 1; j < board.getPieces().length + 1; j++) {
                if (board.getPieces()[8 - i][j - 1] != null && board.getPieces()[8 - i][j - 1].getTeamColor() != teamColor) {
                    ChessPosition position = new ChessPosition(i, j);
                    ChessPiece piece = board.getPiece(position);
                    possibleMoves.addAll(piece.pieceMoves(board, position));

                }
            }
            for (ChessMove move : possibleMoves) {
                ChessPiece piece = board.getPiece(move.getEndPosition());
                if (board.getPieces()[8 - move.getEndPosition().getRow()][move.getEndPosition().getColumn() - 1] != null && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    Collection<ChessMove> kingMoves = new ArrayList<>();
                    for (int k = 1; k < board.getPieces().length + 1; k++) {
                        for (int j = 1; j < board.getPieces().length + 1; j++) {
                            if (board.getPieces()[8 - k][j - 1] != null) {
                                ChessPosition position = new ChessPosition(k, j);
                                if (board.getPiece(position).getPieceType() == ChessPiece.PieceType.KING && board.getPiece(position).getTeamColor() == teamColor) {
                                    inCheck = true;
                                    kingMoves = board.getPiece(position).pieceMoves(board, position);
                                }
                            }
                        }
                    }
                    for(ChessMove kingMove: kingMoves){

                    }

                }
            }
        }
        return inCheck;
    }

    private boolean willBeInCheck(TeamColor teamColor, ChessMove move) {
        board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));
        board.addPiece(move.getStartPosition(), null);
        ;
        for (int i = 1; i < board.getPieces().length + 1; i++) {
            Collection<ChessMove> possibleMoves = new ArrayList<>();
            for (int j = 1; j < board.getPieces().length + 1; j++) {
                if (board.getPieces()[8 - i][j - 1] != null && board.getPieces()[8 - i][j - 1].getTeamColor() != teamColor) {
                    ChessPosition position = new ChessPosition(i, j);
                    ChessPiece piece = board.getPiece(position);
                    possibleMoves.addAll(piece.pieceMoves(board, position));
                }
            }
            for (ChessMove current_move : possibleMoves) {
                ChessPiece piece = board.getPiece(current_move.getEndPosition());
                if (board.getPieces()[8 - current_move.getEndPosition().getRow()][current_move.getEndPosition().getColumn() - 1] != null && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    ChessMove backMove = new ChessMove(move.getEndPosition(), move.getStartPosition(), move.getPromotionPiece());
                    board.addPiece(backMove.getEndPosition(), board.getPiece(backMove.getStartPosition()));
                    board.addPiece(backMove.getStartPosition(), null);
                    return true;
                }
            }
        }
        ChessMove backMove = new ChessMove(move.getEndPosition(), move.getStartPosition(), move.getPromotionPiece());
        board.addPiece(backMove.getEndPosition(), board.getPiece(backMove.getStartPosition()));
        board.addPiece(backMove.getStartPosition(), null);
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        Collection<ChessMove> kingMoves = new ArrayList<>();
        boolean isStalemate = false;
        for (int i = 1; i < board.getPieces().length + 1; i++) {
            for (int j = 1; j < board.getPieces().length + 1; j++) {
                if (board.getPieces()[8 - i][j - 1] != null) {
                    ChessPosition position = new ChessPosition(i, j);
                    if (board.getPiece(position).getPieceType() == ChessPiece.PieceType.KING && board.getPiece(position).getTeamColor() == teamColor) {
                        kingMoves = board.getPiece(position).pieceMoves(board, position);
                    }
                }
            }
        }
        for (ChessMove move : kingMoves) {
            if (!willBeInCheck(teamColor, move)) {
                return false;
            }
        }
        return isStalemate;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */

    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "teamTurn=" + teamTurn +
                ", board=" + board +
                '}';
    }
}
